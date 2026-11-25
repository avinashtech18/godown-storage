import { useState } from 'react'
import './WarehouseMap.css'

const bays = [
  {
    id: 'arrival',
    label: 'Arrival desk',
    description: 'Packages are checked in here before going into storage.',
  },
  {
    id: 'small',
    label: 'Small-item racks',
    description: 'Shelves for bags, boxes, and smaller items.',
  },
  {
    id: 'large',
    label: 'Large-item bays',
    description: 'Floor space for suitcases and larger items.',
  },
  {
    id: 'pickup',
    label: 'Pickup desk',
    description: 'Packages are handed back to customers here.',
  },
]

export default function WarehouseMap({ role }) {
  const [selectedId, setSelectedId] = useState('arrival')
  const [demo, setDemo] = useState(null)
  const selected = bays.find(bay => bay.id === selectedId)

  function playDemo(type) {
    setDemo(previous => ({
      type,
      run: (previous?.run ?? 0) + 1,
    }))
  }

  return (
    <section className="warehouse-map" aria-labelledby="warehouse-map-heading">
      <div className="warehouse-map-header">
        <div>
          <h2 id="warehouse-map-heading">
            {role === 'ADMIN' ? 'Warehouse overview' : 'Explore the warehouse'}
          </h2>
          <p>Sample floor plan. Select a bay to see how it’s used.</p>
        </div>
        <span className="warehouse-demo-label">Demo layout</span>
      </div>

      <div className="warehouse-floor">
        {bays.map(bay => (
          <button
            key={bay.id}
            type="button"
            className="warehouse-bay"
            aria-pressed={bay.id === selectedId}
            onClick={() => setSelectedId(bay.id)}
          >
            {bay.label}
          </button>
        ))}
      </div>

      <p>
        <strong>{selected.label}</strong> · {selected.description}
      </p>

      <div className="warehouse-demo-actions">
        <button type="button" className="secondary-button" onClick={() => playDemo('arrival')}>
          Simulate arrival
        </button>
        <button type="button" className="secondary-button" onClick={() => playDemo('pickup')}>
          Simulate pickup
        </button>
      </div>

      {demo && (
        <>
          <div className="warehouse-track" aria-hidden="true">
            <span
              key={demo.run}
              className={`warehouse-package warehouse-package--${demo.type}`}
            >
              BOX
            </span>
          </div>
          <p role="status">
            {demo.type === 'arrival'
              ? 'Arrival demo · Arrival desk → Storage'
              : 'Pickup demo · Storage → Pickup desk'}
          </p>
        </>
      )}
    </section>
  )
}