import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';

const originalFetch = global.fetch;

describe('App', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
    vi.stubEnv('VITE_LENDING_API_KEY', 'test-lending-key');
  });

  afterAll(() => {
    global.fetch = originalFetch;
    vi.unstubAllEnvs();
  });

  it('validates required fields before submitting', async () => {
    render(<App />);

    await userEvent.click(screen.getByRole('button', { name: /generate quote/i }));

    expect(screen.getByText(/loan amount must be greater than zero/i)).toBeInTheDocument();
  });

  it('shows a successful quote response', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ quoteId: 'QUOTE-10001', commissionRate: 0.015, totalCommission: 1500.0 }),
    }) as typeof fetch;

    render(<App />);

    await userEvent.type(screen.getByLabelText(/loan amount/i), '100000');
    await userEvent.type(screen.getByLabelText(/loan term in months/i), '60');
    await userEvent.click(screen.getByRole('button', { name: /generate quote/i }));

    expect(await screen.findByText('Quote generated')).toBeInTheDocument();
    expect(screen.getByText('QUOTE-10001')).toBeInTheDocument();
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/commission-quotes'),
      expect.objectContaining({
        headers: expect.objectContaining({ 'api-key': 'test-lending-key' }),
      }),
    );
  });

  it('shows an API error message on failure', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ message: 'Vendor timeout' }),
    }) as typeof fetch;

    render(<App />);

    await userEvent.type(screen.getByLabelText(/loan amount/i), '100000');
    await userEvent.type(screen.getByLabelText(/loan term in months/i), '60');
    await userEvent.click(screen.getByRole('button', { name: /generate quote/i }));

    expect(await screen.findByText('Vendor timeout')).toBeInTheDocument();
  });
});
