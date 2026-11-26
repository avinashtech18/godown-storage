import { useState } from 'react'

const baseRates = {
  Small: 100,
  Medium: 250,
  Large: 500,
}

const currency = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
})

export default function RateEstimator() {
  const [size, setSize] = useState('Medium')
  const [weight, setWeight] = useState('')

  const kilograms = Number(weight)
  const validWeight =
    /^\d+(?:\.\d{1,2})?$/.test(weight) &&
    Number.isFinite(kilograms) &&
    kilograms > 0

  return (
    <section aria-labelledby="rate-estimator-heading">
      <h3 id="rate-estimator-heading">Estimate your monthly cost</h3>

      <label>
        Package size
        <select value={size} onChange={event => setSize(event.target.value)}>
          {Object.keys(baseRates).map(option => (
            <option key={option} value={option}>{option}</option>
          ))}
        </select>
      </label>

      <label>
        Package weight (kg)
        <input
          type="number"
          min="0.01"
          step="0.01"
          value={weight}
          onChange={event => setWeight(event.target.value)}
        />
      </label>

      {weight && !validWeight && (
        <p className="error">Enter a positive weight with up to two decimal places.</p>
      )}

      <p role="status">
        {validWeight
          ? `${currency.format(baseRates[size] + kilograms * 5)} per month`
          : 'Enter a weight to see your estimate.'}
      </p>
    </section>
  )
}