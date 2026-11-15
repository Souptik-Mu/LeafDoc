
import { useState, type ReactNode } from 'react'

import {
  Activity,
  ArrowUpRight,
  BookOpen,
  ChevronRight,
  Clock3,
  Flower2,
  Heart,
  Home,
  Leaf,
  Plus,
  Settings,
  Sprout,
  UploadCloud,
  UserRound,
} from 'lucide-react'

import {
  BrowserRouter,
  Navigate,
  NavLink,
  Route,
  Routes,
} from 'react-router-dom'

import './App.css'

import leafdocIcon from './assets/LEAFDOC ICON IMAGE.jpeg'
import overviewImage from './assets/OVERVIEW PAGE IMAGE 1.jpg'
import myGardenImage from './assets/OVERVIEW PAGE - MY GARDEN IMAGE.jpg'
import myGardenPageImage from './assets/MY GARDEN PAGE IMAGE.jpg'
import leafAnalysisImage from './assets/LEAF ANALYSIS PAGE IMAGE.jpg'
import leafAnalysisStagesImage from './assets/LEAF ANALYSIS PAGE.jpg'
import pastAnalysesImage from './assets/PAST ANALYSES IMAGE OVERVIEW PAGE.jpg'
import analysisHistoryImage from './assets/ANALYSIS HISTORY PAGE IMAGE.jpg'
import favouritesImage from './assets/FAVOURITES.jpeg'
import profileImage from './assets/PROFILE.jpeg'
import settingsImage from './assets/SETTINGS.jpeg'

const navigation = [
  { label: 'Overview', path: '/dashboard', icon: Home },
  { label: 'My Garden', path: '/garden', icon: Sprout },
  { label: 'Leaf Analysis', path: '/analysis', icon: Activity },
  { label: 'Analysis History', path: '/history', icon: Clock3 },
]

const personalNavigation = [
  { label: 'Favourites', path: '/favourites', icon: Heart },
  { label: 'My Profile', path: '/profile', icon: UserRound },
  { label: 'Settings', path: '/settings', icon: Settings },
]

function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)

  const closeMenu = () => setMenuOpen(false)

  return (
    <div className="app-shell">
      <aside className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}>
        <NavLink to="/dashboard" className="brand" onClick={closeMenu}>
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
                onClick={closeMenu}
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
                onClick={closeMenu}
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

      <main className="main-area">
        <header className="topbar">
          <button
            className="mobile-menu"
            onClick={() => setMenuOpen(!menuOpen)}
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

        <div className="page-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            <Route path="/dashboard" element={<Dashboard />} />

            <Route
              path="/garden"
              element={
                <SimplePage
                  title="My Garden"
                  subtitle="A home for every plant you care for."
                  icon={<Sprout />}
                  bannerImage={myGardenPageImage}
                  bannerAlt="A beautiful rooftop garden with potted plants and a cosy seating area"
                />
              }
            />

            <Route
              path="/analysis"
              element={
                <SimplePage
                  title="Leaf Analysis"
                  subtitle="Upload a leaf photo to explore its health and possible disease."
                  icon={<UploadCloud />}
                  bannerImage={leafAnalysisStagesImage}
                  bannerAlt="Four illustrative stages of leaf health: affected, under treatment, improving and healthy"
                  bannerTitle={
                    <>
                      Understand every
                      <br />
                      stage of leaf health.
                    </>
                  }
                  bannerLabel="FROM FIRST CHECK TO BETTER CARE"
                />
              }
            />

            <Route
              path="/history"
              element={
                <SimplePage
                  title="Analysis History"
                  subtitle="Your previous plant analyses, all in one place."
                  icon={<BookOpen />}
                  bannerImage={analysisHistoryImage}
                  bannerAlt="A collection of different indoor plants arranged together in natural sunlight"
                />
              }
            />

            <Route
              path="/favourites"
              element={
                <SimplePage
                  title="Favourites"
                  subtitle="Keep your favourite plant information close."
                  icon={<Heart />}
                  bannerImage={favouritesImage}
                  bannerAlt="A beautiful collection of favourite potted plants in a bright home garden"
                />
              }
            />

            <Route
              path="/profile"
              element={
                <SimplePage
                  title="My Profile"
                  subtitle="Your personal LEAFDOC account."
                  icon={<UserRound />}
                  bannerImage={profileImage}
                  bannerAlt="A peaceful plant-inspired profile banner"
                />
              }
            />

            <Route
              path="/settings"
              element={
                <SimplePage
                  title="Settings"
                  subtitle="Manage your account preferences."
                  icon={<Settings />}
                  bannerImage={settingsImage}
                  bannerAlt="A plant-inspired settings page banner"
                />
              }
            />

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </main>
    </div>
  )
}

function Dashboard() {
  return (
    <section className="dashboard">
      <div className="welcome-row">
        <div>
          <p className="eyebrow">
            <span className="eyebrow-line" />
            YOUR PERSONAL GARDEN COMPANION
          </p>

          <h1>
            Let your garden
            <br />
            <em>thrive.</em>
          </h1>

          <p className="welcome-description">
            A little knowledge, a little care, and a whole lot of green.
            Welcome to your plant care space.
          </p>
        </div>

        <div className="welcome-date">
          <span className="date-sun">☀</span>
          <span>MAKE TODAY A GREEN DAY</span>
        </div>
      </div>

      <div className="feature-banner overview-banner">
        <img
          className="feature-image"
          src={overviewImage}
          alt="A single green indoor plant in a ceramic pot, lit by natural sunlight"
        />

        <div className="feature-overlay" />

        <div className="feature-content">
          <span className="feature-kicker">
            <Leaf size={15} />
            A LITTLE MORE ABOUT YOUR PLANTS
          </span>

          <h2>
            Every leaf has
            <br />
            a story to tell.
          </h2>

          <p>
            Discover what your plants are telling you. Explore their health,
            understand their needs, and find the right care to help your
            home garden flourish.
          </p>

          <NavLink to="/analysis" className="feature-button">
            Analyse a leaf <ArrowUpRight size={17} />
          </NavLink>
        </div>

        <div className="feature-caption">
          GROW WITH CARE <span>•</span> GROW WITH CONFIDENCE
        </div>
      </div>

      <div className="section-heading">
        <div>
          <p className="eyebrow">EXPLORE LEAFDOC</p>
          <h2>Your garden journey</h2>
        </div>

        <span className="section-note">
          A little progress every day.
        </span>
      </div>

      <div className="feature-grid">
        <NavLink to="/garden" className="visual-card garden-card">
          <img
            src={myGardenImage}
            alt="A collection of plants in a home garden"
          />

          <div className="visual-card-shade" />

          <div className="visual-card-content">
            <span className="visual-card-icon">
              <Sprout size={19} />
            </span>

            <div>
              <span className="visual-card-label">YOUR GREEN CORNER</span>
              <h3>My Garden</h3>
              <p>Keep your plants organised and close.</p>
            </div>

            <span className="visual-card-arrow">
              <ArrowUpRight size={19} />
            </span>
          </div>
        </NavLink>

        <NavLink to="/analysis" className="visual-card analysis-card">
          <img src={leafAnalysisImage} alt="Colourful green leaves" />

          <div className="visual-card-shade" />

          <div className="visual-card-content">
            <span className="visual-card-icon">
              <Activity size={19} />
            </span>

            <div>
              <span className="visual-card-label">
                LISTEN TO YOUR LEAVES
              </span>
              <h3>Leaf Analysis</h3>
              <p>Explore what your leaf might be telling you.</p>
            </div>

            <span className="visual-card-arrow">
              <ArrowUpRight size={19} />
            </span>
          </div>
        </NavLink>

        <NavLink to="/history" className="visual-card history-card">
          <img
            src={pastAnalysesImage}
            alt="Past plant analyses and plant care records"
          />

          <div className="visual-card-shade" />

          <div className="visual-card-content">
            <span className="visual-card-icon">
              <BookOpen size={19} />
            </span>

            <div>
              <span className="visual-card-label">
                YOUR GROWTH JOURNEY
              </span>
              <h3>Past Analyses</h3>
              <p>Revisit your previous plant check-ups.</p>
            </div>

            <span className="visual-card-arrow">
              <ArrowUpRight size={19} />
            </span>
          </div>
        </NavLink>
      </div>

      <div className="bottom-feature">
        <div className="bottom-feature-icon">
          <Flower2 size={23} />
        </div>

        <div>
          <strong>Rooted in care. Growing with knowledge.</strong>
          <p>
            Thoughtful plant care begins with understanding what your plants need.
          </p>
        </div>

        <Leaf className="bottom-leaf" size={44} />
      </div>
    </section>
  )
}

type SimplePageProps = {
  title: string
  subtitle: string
  icon: ReactNode
  bannerImage?: string
  bannerAlt?: string
  bannerTitle?: ReactNode
  bannerLabel?: string
}

function SimplePage({
  title,
  subtitle,
  icon,
  bannerImage,
  bannerAlt,
  bannerTitle,
  bannerLabel,
}: SimplePageProps) {
  return (
    <section className="simple-page">
      <div className="simple-page-heading">
        <div className="simple-page-icon">{icon}</div>

        <div>
          <p className="eyebrow">YOUR LEAFDOC SPACE</p>
          <h1>{title}</h1>
          <p className="welcome-description">{subtitle}</p>
        </div>
      </div>

      {bannerImage && (
        <div
          className={`simple-page-banner ${
            title === 'Leaf Analysis'
              ? 'analysis-stage-banner'
              : title === 'Analysis History'
                ? 'history-page-banner'
                : title === 'Favourites'
                  ? 'favourites-page-banner'
                  : title === 'My Profile'
                    ? 'profile-page-banner'
                    : title === 'Settings'
                      ? 'settings-page-banner'
                      : ''
          }`}
        >
          <img
            src={bannerImage}
            alt={bannerAlt ?? 'Green plants growing in natural light'}
          />

          {title !== 'Leaf Analysis' && (
            <>
              <div className="simple-page-banner-overlay" />

              <div className="simple-page-banner-copy">
                <span>
                  {title === 'Analysis History'
                    ? 'YOUR PLANTS, YOUR JOURNEY'
                    : title === 'Favourites'
                      ? 'A COLLECTION CLOSE TO YOUR HEART'
                      : title === 'My Profile'
                        ? 'YOUR PERSONAL LEAFDOC SPACE'
                        : title === 'Settings'
                          ? 'YOUR PREFERENCES, YOUR WAY'
                          : 'GROW AT YOUR OWN PACE'}
                </span>
                <h2>
                  {title === 'Analysis History' ? (
                    <>
                      Every plant has a story
                      <br />
                      worth remembering.
                    </>
                  ) : title === 'Favourites' ? (
                    <>
                      The plants you love,
                      <br />
                      all in one place.
                    </>
                  ) : title === 'My Profile' ? (
                    <>
                      Your space,
                      <br />
                      your gardening journey.
                    </>
                  ) : title === 'Settings' ? (
                    <>
                      Make your space
                      <br />
                      feel just right.
                    </>
                  ) : (
                    <>
                      Every plant deserves
                      <br />
                      a little attention.
                    </>
                  )}
                </h2>
              </div>
            </>
          )}
        </div>
      )}

      {title === 'Leaf Analysis' && (
        <div className="analysis-stage-caption">
          <span>{bannerLabel}</span>
          <h2>{bannerTitle}</h2>
          <p>
            Notice the changes, understand your plant's needs, and give it
            thoughtful care at every stage.
          </p>
        </div>
      )}

      <div className="simple-page-note">
        <Plus size={19} />

        <div>
          <strong>Let's build your green space.</strong>
          <p>
            This section's features will be added next, based on the functions
            available in the LEAFDOC project.
          </p>
        </div>
      </div>
    </section>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  )
}

export default App
