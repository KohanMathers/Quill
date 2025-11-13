export interface Env {
  SESSIONS: DurableObjectNamespace;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);
    
    const corsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
    };
    
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: corsHeaders });
    }

    url.pathname = url.pathname.replace(/\/+$/, '') || '/';

    if (url.pathname === '/session' && request.method === 'POST') {
      const content = await request.text();
      const sessionId = generateSessionId();
      
      const id = env.SESSIONS.idFromName(sessionId);
      const stub = env.SESSIONS.get(id);
      
      await stub.fetch('http://internal/init', {
        method: 'POST',
        body: content,
      });
      
      return new Response(JSON.stringify({ sessionId }), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      });
    }

    if (request.method === "DELETE" && url.pathname.match(/^\/session\/([a-z0-9]{8})$/)) {
      const sessionId = url.pathname.split("/")[2];

      const id = env.SESSIONS.idFromName(sessionId);
      const stub = env.SESSIONS.get(id);

      await stub.fetch("http://internal/delete", { method: "POST" });

      return new Response("Deleted", { headers: corsHeaders });
    }

    const match = url.pathname.match(/^\/session\/([a-z0-9]{8})(\/.*)?$/);
    if (match) {
      const sessionId = match[1];
      const id = env.SESSIONS.idFromName(sessionId);
      const stub = env.SESSIONS.get(id);
      
      const response = await stub.fetch(request);
      
      const newHeaders = new Headers(response.headers);
      Object.entries(corsHeaders).forEach(([k, v]) => newHeaders.set(k, v));
      
      return new Response(response.body, {
        status: response.status,
        headers: newHeaders,
      });
    }

    return new Response('Not Found', { status: 404 });
  },
};

export class EditorSession {
  private content: string = '';
  private editedContent: string | null = null;
  private waitingRequest: ((content: string) => void) | null = null;
  private createdAt: number = Date.now();
  private lastActivity: number = Date.now();

  constructor(private state: DurableObjectState) {
    this.state.blockConcurrencyWhile(async () => {
      const checkInterval = setInterval(() => {
        if (Date.now() - this.lastActivity > 30 * 60 * 1000) {
          clearInterval(checkInterval);
        }
      }, 60 * 1000);
    });
  }

  async fetch(request: Request): Promise<Response> {
    const url = new URL(request.url);
    this.lastActivity = Date.now();

    if (url.pathname === '/init' && request.method === 'POST') {
      this.content = await request.text();
      return new Response('OK');
    }

    if (url.pathname === '/delete' && request.method === 'POST') {
      this.content = '';
      this.editedContent = null;
      this.waitingRequest = null;

      await this.state.storage.deleteAll();

      return new Response('Deleted');
    }

    if (url.pathname.endsWith('/wait') && request.method === 'GET') {
      if (this.editedContent !== null) {
        const content = this.editedContent;
        this.editedContent = null;
        return new Response(content);
      }

      return new Promise((resolve) => {
        const timeout = setTimeout(() => {
          this.waitingRequest = null;
          resolve(new Response('', { status: 204 })); // No content yet
        }, 25000);

        this.waitingRequest = (content: string) => {
          clearTimeout(timeout);
          this.waitingRequest = null;
          resolve(new Response(content));
        };
      });
    }

    if (request.method === 'GET') {
      if (!this.content) {
        return new Response('Session not found', { status: 404 });
      }
      return new Response(this.content);
    }

    if (request.method === 'POST') {
      const editedContent = await request.text();
      this.editedContent = editedContent;

      if (this.waitingRequest) {
        this.waitingRequest(editedContent);
        this.editedContent = null;
      }

      return new Response('OK');
    }

    return new Response('Method not allowed', { status: 405 });
  }
}

function generateSessionId(): string {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < 8; i++) {
    result += chars[Math.floor(Math.random() * chars.length)];
  }
  return result;
}
