
import { ChevronRight, Flower2 } from 'lucide-react'
import { NavLink } from 'react-router-dom'

import leafdocIcon from '../assets/LEAFDOC ICON IMAGE.jpeg'
import { navigation, personalNavigation } from '../data/NavigationData'

type SidebarProps = {
  menuOpen: boolean
  onNavigate: () => void
}

function Sidebar({ menuOpen, onNavigate }: SidebarProps) {
  return (
    <aside className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}>
      <NavLink to="/dashboard" className="brand" onClick={onNavigate}>
        <span className="brand-mark">
          <img src={leafdocIcon} alt="LeafDoc logo" />
        </span>
        <span className="brand-name">LeafDoc</span>
      </NavLink>

      <div className="sidebar-section">
        <p className="nav-label">MENU</p>

        <nav className="side-nav">
          {navigation.map(({ label, path, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }
              onClick={onNavigate}
            >
              <Icon size={19} strokeWidth={1.8} />
              <span>{label}</span>
              {path === '/analysis' && (
                <span className="nav-new">NEW</span>
              )}
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="sidebar-section personal-section">
        <p className="nav-label">PERSONAL</p>

        <nav className="side-nav">
          {personalNavigation.map(({ label, path, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }
              onClick={onNavigate}
            >
              <Icon size={19} strokeWidth={1.8} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="sidebar-bottom">
        <div className="sidebar-promo">
          <div className="promo-icon">
            <Flower2 size={22} />
          </div>
          <p className="promo-title">A little care goes a long way.</p>
          <p className="promo-copy">
            Give your plants the attention they deserve.
          </p>
        </div>

        <div className="sidebar-user">
          <div className="user-avatar">P</div>
          <div>
            <strong>Plant Lover</strong>
            <span>My LEAFDOC space</span>
          </div>
          <ChevronRight size={17} />
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
