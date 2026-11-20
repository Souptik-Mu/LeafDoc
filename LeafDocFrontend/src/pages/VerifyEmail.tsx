import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { CheckCircle2, Leaf, LoaderCircle, XCircle } from 'lucide-react'
import { verifyEmail } from '../services/authService'
import './Auth.css'

function VerifyEmail() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('Verifying your email address...')

  useEffect(() => {
    const token = params.get('token')
    if (!token) {
      setStatus('error')
      setMessage('The verification link is missing its token.')
      return
    }

    verifyEmail(token)
      .then(() => {
        setStatus('success')
        setMessage('Your email has been verified. Your LeafDoc account is ready.')
        window.setTimeout(() => navigate('/dashboard', { replace: true }), 1600)
      })
      .catch(err => {
        setStatus('error')
        setMessage(err instanceof Error ? err.message : 'This verification link is invalid or has expired.')
      })
  }, [params, navigate])

  return (
    <main className="verify-page">
      <div className="verify-card">
        <div className="auth-mobile-brand"><Leaf size={22} /> LEAFDOC</div>
        <div className={`verify-icon ${status}`}>
          {status === 'loading' && <LoaderCircle className="spin" size={34} />}
          {status === 'success' && <CheckCircle2 size={34} />}
          {status === 'error' && <XCircle size={34} />}
        </div>
        <span className="auth-kicker">EMAIL VERIFICATION</span>
        <h2>{status === 'success' ? 'You’re all set!' : status === 'error' ? 'Verification failed' : 'Verifying your email'}</h2>
        <p className="auth-subtitle">{message}</p>
        {status === 'error' && <Link className="auth-submit auth-submit-link" to="/register">Try creating an account again</Link>}
        {status === 'success' && <p className="verify-note">Taking you to your dashboard...</p>}
        {status === 'loading' && <p className="verify-note">Please keep this page open for a moment.</p>}
      </div>
    </main>
  )
}

export default VerifyEmail
