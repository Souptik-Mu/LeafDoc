import {
  Activity,
  Clock3,
  Heart,
  Home,
  Settings,
  Sprout,
  UserRound,
  type LucideIcon,
} from 'lucide-react'

export type NavigationItem = {
  label: string
  path: string
  icon: LucideIcon
}

export const navigation: NavigationItem[] = [
  { label: 'Overview', path: '/dashboard', icon: Home },
  { label: 'My Garden', path: '/garden', icon: Sprout },
  { label: 'Leaf Analysis', path: '/analysis', icon: Activity },
  { label: 'Analysis History', path: '/history', icon: Clock3 },
]

export const personalNavigation: NavigationItem[] = [
  { label: 'Favourites', path: '/favourites', icon: Heart },
  { label: 'My Profile', path: '/profile', icon: UserRound },
  { label: 'Settings', path: '/settings', icon: Settings },
]