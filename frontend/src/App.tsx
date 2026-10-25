import { FormEvent, useMemo, useState } from 'react';

type RiskBand = 'A' | 'B' | 'C' | 'D';

type QuoteRequest = {
  loanAmount: string;
  loanTermInMonths: string;
  riskBand: RiskBand;
};

type QuoteResult = {
  quoteId: string;
  commissionRate: number;
  totalCommission: number;
};

const initialForm: QuoteRequest = {
  loanAmount: '',
  loanTermInMonths: '',
  riskBand: 'B',
};

function App() {
  const [form, setForm] = useState<QuoteRequest>(initialForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<QuoteResult | null>(null);
  const [apiError, setApiError] = useState<string>('');

  const backendUrl = useMemo(() => {
    return import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080';
  }, []);
  const lendingApiKey = import.meta.env.VITE_LENDING_API_KEY ?? 'dev-lending-api-key-123';

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    const loanAmount = Number(form.loanAmount);
    const term = Number(form.loanTermInMonths);

    if (!form.loanAmount || Number.isNaN(loanAmount) || loanAmount <= 0) {
      nextErrors.loanAmount = 'Loan amount must be greater than zero.';
    } else if (loanAmount > 10000000) {
      nextErrors.loanAmount = 'Loan amount must be no more than 10,000,000.';
    }

    if (!form.loanTermInMonths || Number.isNaN(term) || term < 1 || term > 360) {
      nextErrors.loanTermInMonths = 'Loan term must be between 1 and 360 months.';
    }

    if (!['A', 'B', 'C', 'D'].includes(form.riskBand)) {
      nextErrors.riskBand = 'Risk band must be A, B, C, or D.';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setApiError('');
    setResult(null);

    if (!validate()) {
      return;
    }

    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch(`${backendUrl}/api/commission-quotes`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'api-key': lendingApiKey,
        },
        body: JSON.stringify({
          loanAmount: Number(form.loanAmount),
          loanTermInMonths: Number(form.loanTermInMonths),
          riskBand: form.riskBand,
        }),
      });

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const message = data?.message ?? 'Unable to generate a commission quote.';
        throw new Error(message);
      }

      const quote = {
        quoteId: data.quoteId,
        commissionRate: Number(data.commissionRate),
        totalCommission: Number(data.totalCommission),
      };
      setResult(quote);
      setApiError('');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'The request failed.';
      setApiError(message);
      setResult(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  const onChange = (field: keyof QuoteRequest, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  return (
    <main className="page-shell">
      <section className="card" aria-live="polite">
        <header className="header-block">
          <p className="eyebrow">Lending platform</p>
          <h1>Lending Platform - Commission Quote</h1>
        </header>

        <form onSubmit={onSubmit} noValidate>
          <div className="field-group">
            <label htmlFor="loanAmount">Loan amount</label>
            <input
              id="loanAmount"
              name="loanAmount"
              type="number"
              min="0.01"
              step="0.01"
              value={form.loanAmount}
              onChange={(event) => onChange('loanAmount', event.target.value)}
              disabled={isSubmitting}
              aria-invalid={Boolean(errors.loanAmount)}
            />
            {errors.loanAmount && <span className="error-text">{errors.loanAmount}</span>}
          </div>

          <div className="field-group">
            <label htmlFor="loanTermInMonths">Loan term in months</label>
            <input
              id="loanTermInMonths"
              name="loanTermInMonths"
              type="number"
              min="1"
              max="360"
              step="1"
              value={form.loanTermInMonths}
              onChange={(event) => onChange('loanTermInMonths', event.target.value)}
              disabled={isSubmitting}
              aria-invalid={Boolean(errors.loanTermInMonths)}
            />
            {errors.loanTermInMonths && <span className="error-text">{errors.loanTermInMonths}</span>}
          </div>

          <div className="field-group">
            <label htmlFor="riskBand">Risk band</label>
            <select
              id="riskBand"
              name="riskBand"
              value={form.riskBand}
              onChange={(event) => onChange('riskBand', event.target.value as RiskBand)}
              disabled={isSubmitting}
            >
              <option value="A">A</option>
              <option value="B">B</option>
              <option value="C">C</option>
              <option value="D">D</option>
            </select>
            {errors.riskBand && <span className="error-text">{errors.riskBand}</span>}
          </div>

          <div className="actions">
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Generating quote…' : 'Generate Quote'}
            </button>
            {apiError && (
              <button type="button" className="secondary" onClick={onSubmit}>
                Retry
              </button>
            )}
          </div>

          {isSubmitting && <div className="status-message">Request in progress…</div>}

          {apiError && <div className="alert error">{apiError}</div>}

          {result && (
            <div className="success-panel" role="status" aria-live="polite">
              <h2>Quote generated</h2>
              <dl>
                <div>
                  <dt>Quote ID</dt>
                  <dd>{result.quoteId}</dd>
                </div>
                <div>
                  <dt>Commission rate</dt>
                  <dd>{(result.commissionRate * 100).toFixed(2)}%</dd>
                </div>
                <div>
                  <dt>Total commission</dt>
                  <dd>${result.totalCommission.toFixed(2)}</dd>
                </div>
              </dl>
              <button type="button" className="secondary" onClick={() => setResult(null)}>
                Submit another quote
              </button>
            </div>
          )}
        </form>
      </section>
    </main>
  );
}

export default App;
