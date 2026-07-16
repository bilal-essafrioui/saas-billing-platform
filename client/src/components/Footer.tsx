

export default function Footer() {
  return (
    <footer className="border-t border-[var(--border)] px-8 py-8">
      <div className="mx-auto flex max-w-[1600px] flex-col items-center justify-between gap-4 text-sm text-[var(--text-secondary)] sm:flex-row">
        <span>© 2026 BillFlow Inc. All rights reserved.</span>
        <div className="flex items-center gap-6">
          <a href="#privacy" className="hover:text-[var(--text-primary)]">
            Privacy Policy
          </a>
          <a href="#terms" className="hover:text-[var(--text-primary)]">
            Terms of Service
          </a>
          <a href="#support" className="hover:text-[var(--text-primary)]">
            Contact Support
          </a>
        </div>
      </div>
    </footer>
  );
}