import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Leaf, Mail, Lock, Eye, EyeOff, ArrowRight } from 'lucide-react'
import { login } from '../services/authService'
import './Auth.css'

function Login() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email.trim().toLowerCase(), password)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to log in.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-visual">
        <div className="auth-brand"><span className="brand-mark"><Leaf size={22} /></span> LEAFDOC</div>
        <div className="auth-visual-copy">
          <span className="auth-kicker">A LITTLE MORE CARE, EVERY DAY</span>
          <h1>Healthy plants<br />begin with <em>understanding.</em></h1>
          <p>Know your plants better. Notice the little changes. Give every leaf the care it deserves.</p>
        </div>
        <div className="auth-visual-footer">GROW WITH CONFIDENCE <span>✳</span></div>
      </section>

      <section className="auth-panel">
        <div className="auth-mobile-brand"><Leaf size={22} /> LEAFDOC</div>
        <div className="auth-form-wrap">
          <span className="auth-kicker">WELCOME BACK</span>
          <h2>Sign in to your garden</h2>
          <p className="auth-subtitle">Enter your details to continue caring for your plants.</p>

          <form onSubmit={handleSubmit} className="auth-form">
            <label htmlFor="email">Email address</label>
            <div className="auth-input-wrap">
              <Mail size={18} />
              <input id="email" type="email" placeholder="you@example.com" value={email}
                onChange={e => setEmail(e.target.value)} autoComplete="email" required />
            </div>

            <label htmlFor="password">Password</label>
            <div className="auth-input-wrap">
              <Lock size={18} />
              <input id="password" type={showPassword ? 'text' : 'password'} placeholder="Enter your password"
                value={password} onChange={e => setPassword(e.target.value)}
                autoComplete="current-password" required />
              <button className="password-toggle" type="button" onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}>
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            {error && <div className="auth-error" role="alert">{error}</div>}
            <button className="auth-submit" type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign in'} <ArrowRight size={17} />
            </button>
          </form>

          <p className="auth-switch">New to LeafDoc? <Link to="/register">Create an account</Link></p>
          <p className="auth-legal">By continuing, you agree to use LeafDoc responsibly and keep your account secure.</p>
        </div>
      </section>
    </main>
  )
}

export default Login
