
import { UserRound } from 'lucide-react'
import { NavLink } from 'react-router-dom'

type TopbarProps = {
  onToggleMenu: () => void
  menuOpen: boolean
}

function Topbar({ onToggleMenu, menuOpen }: TopbarProps) {
  return (
    <header className="topbar">
      <button
        className="mobile-menu"
        onClick={onToggleMenu}
        aria-label="Toggle navigation"
        aria-expanded={menuOpen}
      >
        <span />
        <span />
        <span />
      </button>

      <div className="topbar-breadcrumb">
        Your garden <span>/</span> Overview
      </div>

      <div className="topbar-right">
        <span className="topbar-message">
          A greener day starts here
        </span>
        <NavLink to="/profile" className="topbar-avatar">
          <UserRound size={19} />
        </NavLink>
      </div>
    </header>
  )
}

export default Topbar
