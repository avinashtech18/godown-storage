import { useState } from 'react'

export default function AdminStorageUnits({ accessToken, requestJson }) {
  const [saving, setSaving] = useState(false)
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')

  async function createUnit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const fields = new FormData(form)

    setSaving(true)
    setNotice('')
    setError('')

    try {
      const unit = await requestJson('/api/storage-units', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code: fields.get('code').trim(),
          sizeLabel: fields.get('sizeLabel'),
          monthlyBaseCapacityKg: Number(fields.get('monthlyBaseCapacityKg')),
        }),
      })

      form.reset()
      setNotice(`${unit.code} created. Status: ${unit.status}.`)
    } catch (failure) {
      setError(failure.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section aria-labelledby="create-unit-heading">
      <h2 id="create-unit-heading">Create storage unit</h2>

      <form onSubmit={createUnit}>
        <label>
          Unit code
          <input name="code" placeholder="UNIT-003" maxLength={50} required />
        </label>

        <label>
          Size
          <select name="sizeLabel" defaultValue="MEDIUM">
            <option value="SMALL">Small</option>
            <option value="MEDIUM">Medium</option>
            <option value="LARGE">Large</option>
          </select>
        </label>

        <label>
          Weight capacity (kg)
          <input
            name="monthlyBaseCapacityKg"
            type="number"
            min="0.01"
            max="99999999.99"
            step="0.01"
            required
          />
        </label>

        {notice && <p role="status">{notice}</p>}
        {error && <p className="error" role="alert">{error}</p>}

        <button type="submit" disabled={saving}>
          {saving ? 'Creating…' : 'Create unit'}
        </button>
      </form>
    </section>
  )
}