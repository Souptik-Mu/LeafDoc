
import { Navigate, Route, Routes } from 'react-router-dom'
import { BookOpen, Heart, Settings, Sprout, UploadCloud, UserRound } from 'lucide-react'

import Dashboard from '../pages/Dashboard'
import SimplePage from '../pages/SimplePage'

import myGardenPageImage from '../assets/MY GARDEN PAGE IMAGE.jpg'
import leafAnalysisStagesImage from '../assets/LEAF ANALYSIS PAGE.jpg'
import analysisHistoryImage from '../assets/ANALYSIS HISTORY PAGE IMAGE.jpg'
import favouritesImage from '../assets/FAVOURITES.jpeg'
import profileImage from '../assets/PROFILE.jpeg'
import settingsImage from '../assets/SETTINGS.jpeg'

function AppRoutes() {
  return (
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
  )
}

export default AppRoutes
