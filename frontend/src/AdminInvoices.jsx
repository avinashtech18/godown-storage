import { useState } from 'react'

const rupees = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
})

const paymentDateTime = new Intl.DateTimeFormat('en-IN', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Asia/Kolkata',
})

export default function AdminInvoices({ accessToken, requestJson }) {
  const [invoice, setInvoice] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [pendingAction, setPendingAction] = useState(null)

  async function generateInvoice(event) {
    event.preventDefault()
    const fields = new FormData(event.currentTarget)
    const bookingId = String(fields.get('bookingId')).trim()

    setInvoice(null)
    setError('')
    setNotice('')

    if (!/^[1-9]\d*$/.test(bookingId)) {
      setError('Enter a positive whole-number booking ID.')
      return
    }

    setPendingAction('generate')

    try {
      const result = await requestJson(`/api/bookings/${bookingId}/invoices`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      setInvoice(result)
    } catch (failure) {
      setError(failure.message)
    } finally {
      setPendingAction(null)
    }
  }

  async function recordPayment() {
    setError('')
    setNotice('')
    setPendingAction('pay')

    try {
      // Use the booking and invoice IDs from the displayed invoice.
      const path = `/api/bookings/${invoice.bookingId}/invoices/${invoice.id}/pay`
      const paidInvoice = await requestJson(path, {
        method: 'PATCH',
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      setInvoice(paidInvoice)
      setNotice(`Payment recorded for invoice #${paidInvoice.id}.`)
    } catch (failure) {
      setError(failure.message)
    } finally {
      setPendingAction(null)
    }
  }

  return (
    <section aria-labelledby="generate-invoice-heading">
      <h2 id="generate-invoice-heading">Generate invoice</h2>

      <form onSubmit={generateInvoice}>
        <label>
          Booking ID
          <input name="bookingId" inputMode="numeric" required />
        </label>

        <button type="submit" disabled={pendingAction !== null}>
          {pendingAction === 'generate' ? 'Generating…' : 'Generate invoice'}
        </button>
      </form>

      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p role="status">{notice}</p>}

      {invoice && (
        <div>
          <p>
            <strong>Invoice #{invoice.id}</strong> · {invoice.status}
          </p>

          <dl>
            <dt>Booking</dt>
            <dd>#{invoice.bookingId}</dd>
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
            {invoice.paidAt && (
              <>
                <dt>Paid at (India time)</dt>
                <dd>{paymentDateTime.format(new Date(invoice.paidAt))}</dd>
              </>
            )}
          </dl>

          {invoice.status !== 'PAID' && (
            <button
              type="button"
              className="secondary-button"
              disabled={pendingAction !== null}
              onClick={recordPayment}
            >
              {pendingAction === 'pay' ? 'Recording…' : 'Record payment'}
            </button>
          )}
        </div>
      )}
    </section>
  )
}