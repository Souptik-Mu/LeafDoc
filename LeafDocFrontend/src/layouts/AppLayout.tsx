
import { useState } from 'react'

import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import AppRoutes from '../routes/AppRoutes'

function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)

  const closeMenu = () => setMenuOpen(false)

  return (
    <div className="app-shell">
      <Sidebar
        menuOpen={menuOpen}
        onNavigate={closeMenu}
      />

      <main className="main-area">
        <Topbar
          menuOpen={menuOpen}
          onToggleMenu={() => setMenuOpen(!menuOpen)}
        />

        <div className="page-content">
          <AppRoutes />
        </div>
      </main>
    </div>
  )
}

export default AppLayout
