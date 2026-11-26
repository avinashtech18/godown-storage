import { useEffect, useState } from 'react'
import BookingItems from './BookingItems.jsx'
import BookingInvoices from './BookingInvoices.jsx'
import WarehouseMap from './WarehouseMap.jsx'

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
  const [selectedBookingId, setSelectedBookingId] = useState(null)

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
  const selectedBooking = bookings?.find(
    booking => booking.id === selectedBookingId,
  )

  if (selectedBooking) {
    const isClosing = closingId === selectedBooking.id

    return (
      <section aria-labelledby="booking-details-heading">
        <button
          type="button"
          className="secondary-button"
          onClick={() => setSelectedBookingId(null)}
        >
          {history ? 'Back to booking history' : 'Back to my bookings'}
        </button>

        <h2 id="booking-details-heading">Booking #{selectedBooking.id}</h2>

        <dl>
          <dt>Status</dt>
          <dd>{selectedBooking.status}</dd>

          <dt>Storage unit</dt>
          <dd>#{selectedBooking.unitId}</dd>

          <dt>Started</dt>
          <dd>{selectedBooking.startDate}</dd>

          {selectedBooking.endDate && (
            <>
              <dt>Closed</dt>
              <dd>{selectedBooking.endDate}</dd>
            </>
          )}
        </dl>

        {notice && <p role="status">{notice}</p>}
        {closeError && <p className="error" role="alert">{closeError}</p>}

        <BookingItems
          booking={selectedBooking}
          accessToken={accessToken}
          requestJson={requestJson}
        />

        <BookingInvoices
          bookingId={selectedBooking.id}
          accessToken={accessToken}
          requestJson={requestJson}
        />

        <WarehouseMap role="CUSTOMER" />

        {selectedBooking.status === 'ACTIVE' && (
          <button
            type="button"
            className="secondary-button"
            disabled={closingId !== null}
            onClick={() => closeBooking(selectedBooking.id)}
          >
            {isClosing ? 'Closing…' : 'Close booking'}
          </button>
        )}
      </section>
    )
  }
  return (
    <section aria-labelledby="my-bookings-heading">
      <h2 id="my-bookings-heading">{heading}</h2>

      {notice && <p role="status">{notice}</p>}
      {closeError && <p className="error" role="alert">{closeError}</p>}
      {loadError && <p className="error" role="alert">{loadError}</p>}
      {!loadError && bookings === null && <p role="status">Loading bookings…</p>}

      {visibleBookings?.length === 0 && (
        <p>
          {history
            ? 'You have no closed bookings yet.'
            : 'You have no active bookings yet.'}
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

              <button
                type="button"
                className="secondary-button"
                aria-label={`Open booking ${booking.id}`}
                onClick={() => {
                  setNotice('')
                  setCloseError('')
                  setSelectedBookingId(booking.id)
                }}
              >
                Open booking
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}