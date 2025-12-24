import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { FileText, MessageSquare, Sparkles, ArrowRight, Zap, Shield, Search } from "lucide-react";

/**
 * Landing Page Component.
 * <p>
 * This is the first page users see when they visit the application.
 * It features:
 * <ul>
 *   <li>A welcome hero section with value proposition</li>
 *   <li>Feature highlights (Smart Search, Instant Answers, Security)</li>
 *   <li>"Get Started" call-to-action that navigates to the main app (/app)</li>
 * </ul>
 * It uses a decorative background with gradients and floating elements.
 * </p>
 */
const Welcome = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-accent/30 overflow-hidden">
      {/* Decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-3xl" />
        <div className="absolute top-1/2 -left-40 h-96 w-96 rounded-full bg-gradient-to-br from-accent/40 to-primary/10 blur-3xl" />
        <div className="absolute -bottom-40 right-1/4 h-72 w-72 rounded-full bg-gradient-to-br from-primary/15 to-accent/30 blur-3xl" />
      </div>

      <div className="relative z-10 flex min-h-screen flex-col">
        {/* Header */}
        <header className="flex items-center justify-between px-6 py-5 lg:px-12">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-primary/80 shadow-lg shadow-primary/25">
              <FileText className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-display text-xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              DocuChat
            </span>
          </div>
        </header>

        {/* Main content */}
        <main className="flex flex-1 flex-col items-center justify-center px-6 py-12">
          <div className="max-w-3xl text-center">
            {/* Badge */}
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-sm font-medium text-primary backdrop-blur-sm">
              <Sparkles className="h-4 w-4" />
              AI-Powered Document Intelligence
            </div>

            {/* Headline */}
            <h1 className="font-display text-4xl font-bold leading-tight text-foreground sm:text-5xl lg:text-6xl">
              Chat with your{" "}
              <span className="bg-gradient-to-r from-primary via-primary/80 to-accent-foreground bg-clip-text text-transparent">
                documents
              </span>
              <br />
              like never before
            </h1>

            {/* Subtitle */}
            <p className="mx-auto mt-6 max-w-xl text-lg text-muted-foreground">
              Upload PDFs, research papers, or any document. Ask questions and get accurate, 
              context-aware answers with source citations in seconds.
            </p>

            {/* CTA Button */}
            <div className="mt-10">
              <Button 
                size="lg" 
                onClick={() => navigate("/app")}
                className="gap-2 px-8 py-6 text-base shadow-lg shadow-primary/25 transition-all hover:shadow-xl hover:shadow-primary/30"
              >
                Get Started
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {/* Feature cards */}
          <div className="mt-20 grid max-w-4xl gap-4 px-4 sm:grid-cols-3">
            <FeatureCard 
              icon={Search}
              title="Smart Search"
              description="AI understands context and finds exactly what you need"
            />
            <FeatureCard 
              icon={Zap}
              title="Instant Answers"
              description="Get responses in seconds, not minutes"
            />
            <FeatureCard 
              icon={Shield}
              title="Secure & Private"
              description="Your documents stay private and encrypted"
            />
          </div>
        </main>

        {/* Footer */}
        <footer className="px-6 py-6 text-center text-sm text-muted-foreground">
          <p>Made with ❤️ for researchers, students, and professionals</p>
        </footer>
      </div>
    </div>
  );
};

function FeatureCard({ 
  icon: Icon, 
  title, 
  description 
}: { 
  icon: React.ComponentType<{ className?: string }>;
  title: string;
  description: string;
}) {
  return (
    <div className="group rounded-2xl border border-border/50 bg-card/50 p-6 backdrop-blur-sm transition-all hover:border-primary/30 hover:bg-card/80 hover:shadow-lg hover:shadow-primary/5">
      <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 to-accent/20 text-primary transition-transform group-hover:scale-110">
        <Icon className="h-6 w-6" />
      </div>
      <h3 className="font-display font-semibold text-foreground">{title}</h3>
      <p className="mt-1.5 text-sm text-muted-foreground">{description}</p>
    </div>
  );
}

export default Welcome;
