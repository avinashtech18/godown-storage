import { useEffect, useState } from 'react'

const rupees = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
})

export default function BookingInvoices({ bookingId, accessToken, requestJson }) {
  const [expanded, setExpanded] = useState(false)
  const [invoices, setInvoices] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!expanded) return

    const controller = new AbortController()

    requestJson(`/api/bookings/${bookingId}/invoices`, {
      headers: { Authorization: `Bearer ${accessToken}` },
      signal: controller.signal,
    })
      .then(data => {
        if (!controller.signal.aborted) setInvoices(data)
      })
      .catch(failure => {
        if (!controller.signal.aborted) setError(failure.message)
      })

    return () => controller.abort()
  }, [expanded, bookingId, accessToken, requestJson])

  function toggleInvoices() {
    if (!expanded) {
      setInvoices(null)
      setError('')
    }

    setExpanded(!expanded)
  }

  return (
    <div>
      <button
        type="button"
        className="secondary-button"
        aria-expanded={expanded}
        onClick={toggleInvoices}
      >
        {expanded ? 'Hide invoices' : 'View invoices'}
      </button>

      {expanded && (
        <section aria-label={`Invoices for booking ${bookingId}`}>
          {error ? (
            <p className="error" role="alert">{error}</p>
          ) : invoices === null ? (
            <p role="status">Loading invoices…</p>
          ) : invoices.length === 0 ? (
            <p>No invoices have been issued for this booking.</p>
          ) : (
            <ul>
              {invoices.map(invoice => (
                <li key={invoice.id}>
                  <p>
                    <strong>Invoice #{invoice.id}</strong> · {invoice.status}
                  </p>
                  <dl>
                    <dt>Billing month</dt>
                    <dd>{invoice.billingMonth.slice(0, 7)}</dd>
                    <dt>Due date</dt>
                    <dd>{invoice.dueDate}</dd>
                    <dt>Amount</dt>
                    <dd>{rupees.format(invoice.amount)}</dd>
                    <dt>Late fee</dt>
                    <dd>{rupees.format(invoice.lateFee)}</dd>
                    <dt>Total</dt>
                    <dd>{rupees.format(Number(invoice.amount) + Number(invoice.lateFee))}</dd>
                  </dl>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </div>
  )
}