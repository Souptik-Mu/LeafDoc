import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { Leaf, UserRound, Mail, Lock, Eye, EyeOff, ArrowRight, CheckCircle2 } from 'lucide-react'
import { register } from '../services/authService'
import './Auth.css'

function Register() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (password.length < 8) {
      setError('Password must contain at least 8 characters.')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)
    try {
      await register({ name: name.trim(), email: email.trim().toLowerCase(), password })
      setSubmitted(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create your account.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-visual">
        <div className="auth-brand"><span className="brand-mark"><Leaf size={22} /></span> LEAFDOC</div>
        <div className="auth-visual-copy">
          <span className="auth-kicker">YOUR PERSONAL PLANT COMPANION</span>
          <h1>Make room for<br /><em>something green.</em></h1>
          <p>Create your account and start building a healthier, happier garden—one leaf at a time.</p>
        </div>
        <div className="auth-visual-footer">A HEALTHIER GARDEN STARTS HERE <span>✳</span></div>
      </section>

      <section className="auth-panel">
        <div className="auth-mobile-brand"><Leaf size={22} /> LEAFDOC</div>
        <div className="auth-form-wrap">
          {submitted ? (
            <div className="auth-success">
              <span className="success-icon"><CheckCircle2 size={30} /></span>
              <span className="auth-kicker">ONE LAST STEP</span>
              <h2>Check your inbox</h2>
              <p className="auth-subtitle">We've sent a verification link to <strong>{email}</strong>.</p>
              <p className="auth-subtitle">Open that email and click <strong>Verify Email</strong> to activate your LeafDoc account. The link expires in 15 minutes.</p>
              <Link className="auth-submit auth-submit-link" to="/login">Back to sign in <ArrowRight size={17} /></Link>
            </div>
          ) : (
            <>
              <span className="auth-kicker">GET STARTED</span>
              <h2>Create your account</h2>
              <p className="auth-subtitle">Join LeafDoc and give your plants the attention they deserve.</p>
              <form onSubmit={handleSubmit} className="auth-form">
                <label htmlFor="name">Full name</label>
                <div className="auth-input-wrap">
                  <UserRound size={18} />
                  <input id="name" type="text" placeholder="Your name" value={name}
                    onChange={e => setName(e.target.value)} autoComplete="name" maxLength={100} required />
                </div>

                <label htmlFor="email">Email address</label>
                <div className="auth-input-wrap">
                  <Mail size={18} />
                  <input id="email" type="email" placeholder="you@example.com" value={email}
                    onChange={e => setEmail(e.target.value)} autoComplete="email" required />
                </div>

                <label htmlFor="password">Password</label>
                <div className="auth-input-wrap">
                  <Lock size={18} />
                  <input id="password" type={showPassword ? 'text' : 'password'} placeholder="At least 8 characters"
                    value={password} onChange={e => setPassword(e.target.value)}
                    autoComplete="new-password" minLength={8} required />
                  <button className="password-toggle" type="button" onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}>
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>

                <label htmlFor="confirmPassword">Confirm password</label>
                <div className="auth-input-wrap">
                  <Lock size={18} />
                  <input id="confirmPassword" type="password" placeholder="Re-enter your password"
                    value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)}
                    autoComplete="new-password" required />
                </div>

                {error && <div className="auth-error" role="alert">{error}</div>}
                <button className="auth-submit" type="submit" disabled={loading}>
                  {loading ? 'Creating account...' : 'Create account'} <ArrowRight size={17} />
                </button>
              </form>
              <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
              <p className="auth-legal">We'll send you an email to verify your address before activating your account.</p>
            </>
          )}
        </div>
      </section>
    </main>
  )
}

export default Register
