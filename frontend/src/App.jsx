import { useState } from 'react'
import CustomerDashboard from './CustomerDashboard.jsx'
import AdminStorageUnits from './AdminStorageUnits.jsx'
import AdminInvoices from './AdminInvoices.jsx'
import WarehouseMap from './WarehouseMap.jsx'
import PasswordField from './PasswordField.jsx'

async function requestJson(path, options) {
  const response = await fetch(path, options)
  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(data?.message || `Request failed (${response.status}).`)
  }

  if (data === null) {
    throw new Error('The server did not return JSON. Check the Vite API proxy.')
  }

  return data
}

export default function App() {
  const [session, setSession] = useState(null)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [registering, setRegistering] = useState(false)
  const [notice, setNotice] = useState('')

  async function handleLogin(event) {
    event.preventDefault()
    const form = event.currentTarget
    const fields = new FormData(form)

    setError('')
    setNotice('')
    setSubmitting(true)

    try {
      const { accessToken } = await requestJson('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: String(fields.get('email') ?? '').trim(),
          password: String(fields.get('password') ?? ''),
        }),
      })

      const user = await requestJson('/api/auth/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      form.reset()
      setSession({ accessToken, user })
    } catch (failure) {
      setError(failure.message)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRegister(event) {
    event.preventDefault()
    const form = event.currentTarget
    const fields = new FormData(form)
    const password = String(fields.get('password') ?? '')
    const confirmPassword = String(fields.get('confirmPassword') ?? '')

    setError('')
    setNotice('')

    // BCrypt limits passwords to 72 bytes. Some characters use several bytes.
    if (password.length < 8 || new TextEncoder().encode(password).length > 72) {
      setError('Use at least 8 characters and at most 72 UTF-8 bytes.')
      return
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setSubmitting(true)

    try {
      await requestJson('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: String(fields.get('name') ?? '').trim(),
          email: String(fields.get('email') ?? '').trim(),
          password,
        }),
      })

      form.reset()
      setRegistering(false)
      setNotice('Account created. You can now sign in.')
    } catch (failure) {
      setError(failure.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className={`card ${session ? 'dashboard-card' : 'auth-card'}`}>
      <p className="brand">Godown Storage</p>

      {session ? (
        <>
          <h1>
            {session.user.role === 'ADMIN' ? 'Admin dashboard' : 'Customer portal'}
          </h1>
          <p>Signed in as account #{session.user.id}.</p>

          {session.user.role === 'CUSTOMER' && (
            <CustomerDashboard
              accessToken={session.accessToken}
              requestJson={requestJson}
            />
          )}

          {session.user.role === 'ADMIN' && (
            <>
              <WarehouseMap role="ADMIN" />
              <AdminStorageUnits
                accessToken={session.accessToken}
                requestJson={requestJson}
              />
              <AdminInvoices
                accessToken={session.accessToken}
                requestJson={requestJson}
              />
            </>
          )}

          <button
            className="sign-out-button"
            type="button"
            onClick={() => setSession(null)}
          >
            Sign out
          </button>
        </>
      ) : (
        <>
          <h1>{registering ? 'Create account' : 'Sign in'}</h1>
          {notice && !registering && <p role="status">{notice}</p>}

          <form
            key={registering ? 'register' : 'login'}
            onSubmit={registering ? handleRegister : handleLogin}
          >
            {registering && (
              <label>
                Full name
                <input name="name" autoComplete="name" maxLength={100} required />
              </label>
            )}

            <label>
              Email
              <input
                name="email"
                type="email"
                autoComplete="username"
                maxLength={254}
                required
              />
            </label>

            <PasswordField
              label="Password"
              name="password"
              autoComplete={registering ? 'new-password' : 'current-password'}
              aria-describedby={registering ? 'password-help' : undefined}
              minLength={registering ? 8 : undefined}
              maxLength={registering ? 72 : undefined}
              required
            />

            {registering && (
              <>
                <p id="password-help" className="password-help">
                  Use at least 8 characters and at most 72 UTF-8 bytes.
                  Some characters use more than one byte.
                </p>

                <PasswordField
                  label="Confirm password"
                  name="confirmPassword"
                  autoComplete="new-password"
                  maxLength={72}
                  required
                />
              </>
            )}

            {error && <p className="error" role="alert">{error}</p>}

            <button type="submit" disabled={submitting}>
              {submitting ? 'Please wait…' : registering ? 'Create account' : 'Sign in'}
            </button>

            <button
              className="secondary-button"
              type="button"
              disabled={submitting}
              onClick={() => {
                setError('')
                setNotice('')
                setRegistering(current => !current)
              }}
            >
              {registering ? 'Back to sign in' : 'Create a customer account'}
            </button>
          </form>
        </>
      )}
    </main>
  )
}