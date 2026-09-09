

import React from 'react';
import AttributionBox from '../components/AttributionBox/AttributionBox';
import Layout from '@theme/Layout';
import Hero from '../components/Hero/Hero';
import FeaturesSection from '../components/FeaturesSection/FeaturesSection';
import TeamSection from '../components/TeamSection/TeamSection';
import AboutSection from '../components/AboutSection/AboutSection';
import HomePageNavbar from '../components/HomePageNavbar/HomePageNavbar';
import CallToAction from '../components/CallToAction/CallToAction';

export default function Home() {
  return (
    <Layout
      title="Home"
      description="Discover JaKtA, a Kotlin-based agent programming framework">
      <HomePageNavbar />
      <Hero />
      <main> 
        <FeaturesSection />
        <TeamSection  />
        <AboutSection />
          <CallToAction />
      </main>
      <AttributionBox />
    </Layout>
  );
}
