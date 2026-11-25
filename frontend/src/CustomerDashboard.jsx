import { useEffect, useState } from 'react'
import AvailableUnits from './AvailableUnits.jsx'
import MyBookings from './MyBookings.jsx'
import './CustomerDashboard.css'

const pages = [
  { id: 'units', label: 'Available units' },
  { id: 'bookings', label: 'My bookings' },
  { id: 'history', label: 'Booking history' },
  { id: 'rates', label: 'Rates' },
]

function currentPage() {
  const id = window.location.hash.slice(1)
  return pages.find(page => page.id === id)?.id ?? 'units'
}

export default function CustomerDashboard({ accessToken, requestJson }) {
  const [page, setPage] = useState(currentPage)
  const [bookingsVersion, setBookingsVersion] = useState(0)
  const [unitsVersion, setUnitsVersion] = useState(0)

  useEffect(() => {
    function syncPage() {
      setPage(currentPage())
    }

    window.addEventListener('hashchange', syncPage)
    return () => window.removeEventListener('hashchange', syncPage)
  }, [])

  return (
    <div className="customer-dashboard">
      <nav className="customer-navigation" aria-label="Customer dashboard">
        {pages.map(link => (
          <a
            key={link.id}
            href={`#${link.id}`}
            aria-current={page === link.id ? 'page' : undefined}
          >
            {link.label}
          </a>
        ))}
      </nav>

      {page === 'units' && (
        <AvailableUnits
          accessToken={accessToken}
          requestJson={requestJson}
          refreshVersion={unitsVersion}
          onBooked={() => setBookingsVersion(version => version + 1)}
        />
      )}

      {(page === 'bookings' || page === 'history') && (
        <MyBookings
          key={page}
          accessToken={accessToken}
          requestJson={requestJson}
          refreshVersion={bookingsVersion}
          onClosed={() => setUnitsVersion(version => version + 1)}
          history={page === 'history'}
        />
      )}

      {page === 'rates' && (
        <section className="rates-page" aria-labelledby="storage-rates-heading">
          <header className="rates-header">
            <h2 id="storage-rates-heading">Monthly storage rates</h2>
            <p>Each package costs its base rate plus ₹5 per kg per month.</p>
          </header>

          <div className="rate-grid">
            {[
              { size: 'Small', rate: '₹100', weight: 'Up to 25 kg' },
              { size: 'Medium', rate: '₹250', weight: 'Over 25 kg, up to 75 kg' },
              { size: 'Large', rate: '₹500', weight: 'Over 75 kg, up to 100 kg' },
            ].map(({ size, rate, weight }) => (
              <article key={size} className={`rate-card rate-card--${size.toLowerCase()}`}>
                <h3>{size}</h3>
                <p className="rate-price">
                  {rate} <span>base per month</span>
                </p>
                <dl>
                  <dt>Recommended package weight</dt>
                  <dd>{weight}</dd>
                  <dt>Weight charge</dt>
                  <dd>₹5 per kg per month</dd>
                </dl>
              </article>
            ))}
          </div>

          <p className="rate-note">
            Your unit’s listed capacity is the maximum combined weight of all packages
            in the booking. A 12.50 kg package registered as Medium costs{' '}
            <strong>₹312.50 per month</strong>.
          </p>
        </section>
      )}
    </div>
  )
}