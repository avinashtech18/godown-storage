import { useEffect, useState } from 'react'

export default function BookingItems({ booking, accessToken, requestJson }) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div>
      <button
        type="button"
        className="secondary-button"
        aria-expanded={expanded}
        onClick={() => setExpanded(current => !current)}
      >
        {expanded ? 'Hide items' : 'View items'}
      </button>

      {expanded && (
        <ItemPanel
          booking={booking}
          accessToken={accessToken}
          requestJson={requestJson}
        />
      )}
    </div>
  )
}

function ItemPanel({ booking, accessToken, requestJson }) {
  const [items, setItems] = useState(null)
  const [loadError, setLoadError] = useState('')
  const [submitError, setSubmitError] = useState('')
  const [saving, setSaving] = useState(false)
  const path = `/api/bookings/${booking.id}/items`

  useEffect(() => {
    const controller = new AbortController()

    requestJson(path, {
      headers: { Authorization: `Bearer ${accessToken}` },
      signal: controller.signal,
    })
      .then(data => {
        if (!controller.signal.aborted) setItems(data)
      })
      .catch(error => {
        if (!controller.signal.aborted) setLoadError(error.message)
      })

    return () => controller.abort()
  }, [path, accessToken, requestJson])

  async function addItem(event) {
    event.preventDefault()
    const form = event.currentTarget
    const fields = new FormData(form)

    setSaving(true)
    setSubmitError('')

    try {
      const saved = await requestJson(path, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: fields.get('name').trim(),
          description: fields.get('description').trim() || null,
          size: fields.get('size'),
          weightKg: Number(fields.get('weightKg')),
        }),
      })

      setItems(current => [...current, saved])
      form.reset()
    } catch (error) {
      setSubmitError(error.message)
    } finally {
      setSaving(false)
    }
  }

  if (loadError) return <p className="error" role="alert">{loadError}</p>
  if (items === null) return <p role="status">Loading items…</p>

  return (
    <section aria-label={`Items for booking ${booking.id}`}>
      {items.length === 0 ? (
        <p>No items have been added to this booking.</p>
      ) : (
        <ul>
          {items.map(item => (
            <li key={item.id}>
              <strong>{item.name}</strong> · {item.size} · {item.weightKg} kg
              {item.description && <p>{item.description}</p>}
            </li>
          ))}
        </ul>
      )}

      {booking.status === 'ACTIVE' && (
        <form onSubmit={addItem}>
          <label>
            Item name
            <input name="name" maxLength={100} required />
          </label>

          <label>
            Description (optional)
            <input name="description" maxLength={500} />
          </label>

          <label>
            Size
            <select name="size" defaultValue="MEDIUM">
              <option value="SMALL">Small</option>
              <option value="MEDIUM">Medium</option>
              <option value="LARGE">Large</option>
            </select>
          </label>

          <label>
            Weight (kg)
            <input name="weightKg" type="number" min="0.01" step="0.01" required />
          </label>

          {submitError && <p className="error" role="alert">{submitError}</p>}

          <button type="submit" disabled={saving}>
            {saving ? 'Adding…' : 'Add item'}
          </button>
        </form>
      )}
    </section>
  )
}