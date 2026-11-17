
import {
  Activity,
  ArrowUpRight,
  BookOpen,
  Flower2,
  Leaf,
  Sprout,
} from 'lucide-react'
import { NavLink } from 'react-router-dom'

import overviewImage from '../assets/OVERVIEW PAGE IMAGE 1.jpg'
import myGardenImage from '../assets/OVERVIEW PAGE - MY GARDEN IMAGE.jpg'
import leafAnalysisImage from '../assets/LEAF ANALYSIS PAGE IMAGE.jpg'
import pastAnalysesImage from '../assets/PAST ANALYSES IMAGE OVERVIEW PAGE.jpg'

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

export default Dashboard
