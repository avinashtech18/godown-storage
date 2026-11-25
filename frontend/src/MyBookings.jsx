import { useEffect, useState } from 'react'
import BookingItems from './BookingItems.jsx'
import BookingInvoices from './BookingInvoices.jsx'

export default function MyBookings({
  accessToken,
  requestJson,
  refreshVersion,
  onClosed,
  history = false,
}) {
  const [bookings, setBookings] = useState(null)
  const [loadError, setLoadError] = useState('')
  const [closeError, setCloseError] = useState('')
  const [notice, setNotice] = useState('')
  const [closingId, setClosingId] = useState(null)

  useEffect(() => {
    const controller = new AbortController()

    requestJson('/api/bookings', {
      headers: { Authorization: `Bearer ${accessToken}` },
      signal: controller.signal,
    })
      .then(data => {
        if (controller.signal.aborted) return
        setBookings(data)
        setLoadError('')
      })
      .catch(failure => {
        if (!controller.signal.aborted) setLoadError(failure.message)
      })

    return () => controller.abort()
  }, [accessToken, requestJson, refreshVersion])

  async function closeBooking(bookingId) {
    setClosingId(bookingId)
    setCloseError('')
    setNotice('')

    try {
      const closed = await requestJson(`/api/bookings/${bookingId}/close`, {
        method: 'PATCH',
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      setBookings(current =>
        current.map(booking => booking.id === closed.id ? closed : booking),
      )
      setNotice(`Booking #${closed.id} closed. You can find it in Booking history.`)
      onClosed()
    } catch (failure) {
      setCloseError(failure.message)
    } finally {
      setClosingId(null)
    }
  }

  const heading = history ? 'Booking history' : 'My bookings'
  const visibleStatus = history ? 'CLOSED' : 'ACTIVE'

  // Both tabs use the same API. The selected tab determines which status to show.
  const visibleBookings = bookings?.filter(
    booking => booking.status === visibleStatus,
  )

  return (
    <section aria-labelledby="my-bookings-heading">
      <h2 id="my-bookings-heading">{heading}</h2>

      {notice && <p role="status">{notice}</p>}
      {closeError && <p className="error" role="alert">{closeError}</p>}
      {loadError && <p className="error" role="alert">{loadError}</p>}
      {!loadError && bookings === null && <p role="status">Loading bookings…</p>}

      {visibleBookings?.length === 0 && (
        <p>
          {history ? 'You have no closed bookings yet.' : 'You have no active bookings yet.'}
        </p>
      )}

      {visibleBookings?.length > 0 && (
        <ul>
          {visibleBookings.map(booking => (
            <li key={booking.id}>
              <p>
                <strong>Booking #{booking.id}</strong> · {booking.status}
                <br />
                Unit #{booking.unitId} · Started {booking.startDate}
                {booking.endDate && <> · Closed {booking.endDate}</>}
              </p>

              {booking.status === 'ACTIVE' && (
                <button
                  className="secondary-button"
                  type="button"
                  aria-label={`Close booking ${booking.id}`}
                  disabled={closingId !== null}
                  onClick={() => closeBooking(booking.id)}
                >
                  {closingId === booking.id ? 'Closing…' : 'Close booking'}
                </button>
              )}

              <BookingItems
                booking={booking}
                accessToken={accessToken}
                requestJson={requestJson}
              />

              <BookingInvoices
                bookingId={booking.id}
                accessToken={accessToken}
                requestJson={requestJson}
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}