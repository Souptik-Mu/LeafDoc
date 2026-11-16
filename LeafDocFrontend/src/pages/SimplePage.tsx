
import { Plus } from 'lucide-react'

import type { SimplePageProps } from '../types/SimplePage'

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

export default SimplePage
