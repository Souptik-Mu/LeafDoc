import type { ReactNode } from 'react'

export type SimplePageProps = {
  title: string
  subtitle: string
  icon: ReactNode
  bannerImage?: string
  bannerAlt?: string
  bannerTitle?: ReactNode
  bannerLabel?: string
}