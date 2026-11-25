import { useEffect, useState } from 'react'

export default function AvailableUnits({
  accessToken,
  requestJson,
  onBooked,
  refreshVersion,
}) {
  const [units, setUnits] = useState(null)
  const [loadError, setLoadError] = useState('')
  const [bookingError, setBookingError] = useState('')
  const [notice, setNotice] = useState('')
  const [bookingUnitId, setBookingUnitId] = useState(null)

  useEffect(() => {
    const controller = new AbortController()

    requestJson('/api/storage-units/available', {
      headers: { Authorization: `Bearer ${accessToken}` },
      signal: controller.signal,
    })
      .then(data => {
        if (controller.signal.aborted) return
        setUnits(data)
        setLoadError('')
      })
      .catch(failure => {
        if (!controller.signal.aborted) setLoadError(failure.message)
      })

    return () => controller.abort()
  }, [accessToken, requestJson, refreshVersion])

  async function bookUnit(unit) {
    setBookingUnitId(unit.id)
    setBookingError('')
    setNotice('')

    try {
      const booking = await requestJson('/api/bookings', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ unitId: unit.id }),
      })

      setUnits(current => current.filter(available => available.id !== unit.id))
      setNotice(`${unit.code} booked. Your booking ID is ${booking.id}.`)
      onBooked()
    } catch (failure) {
      setBookingError(failure.message)
    } finally {
      setBookingUnitId(null)
    }
  }

  return (
    <section aria-labelledby="available-units-heading">
      <h2 id="available-units-heading">Available storage units</h2>

      {notice && <p role="status">{notice}</p>}
      {bookingError && <p className="error" role="alert">{bookingError}</p>}

      {loadError ? (
        <p className="error" role="alert">{loadError}</p>
      ) : units === null ? (
        <p role="status">Loading storage units…</p>
      ) : units.length === 0 ? (
        <p>No storage units are available right now.</p>
      ) : (
        <ul>
          {units.map(unit => (
            <li key={unit.id}>
              <p>
                <strong>{unit.code}</strong>
                {' · '}{unit.sizeLabel}
                {' · '}{unit.monthlyBaseCapacityKg} kg capacity
              </p>
              <button
                type="button"
                aria-label={`Book unit ${unit.code}`}
                disabled={bookingUnitId !== null}
                onClick={() => bookUnit(unit)}
              >
                {bookingUnitId === unit.id ? 'Booking…' : 'Book unit'}
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}