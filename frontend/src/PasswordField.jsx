import { useState } from 'react'

export default function PasswordField({ label, ...inputProps }) {
  const [visible, setVisible] = useState(false)

  return (
    <div className="password-field">
      <label>
        {label}
        <input {...inputProps} type={visible ? 'text' : 'password'} />
      </label>

      <button
        type="button"
        className="password-toggle"
        aria-label={`${visible ? 'Hide' : 'Show'} ${label.toLowerCase()}`}
        onClick={() => setVisible(current => !current)}
      >
        {visible ? 'Hide' : 'Show'}
      </button>
    </div>
  )
}