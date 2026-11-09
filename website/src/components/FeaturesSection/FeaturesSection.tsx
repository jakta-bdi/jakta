import React from 'react';
import Heading from '@theme/Heading';
import styles from './FeaturesSection.module.css';

function InfoCard() {
  return (
    <div className={styles.infoSection}>
      <div className={styles.infoText}>
        <Heading as="h2" className="section-title">What is <span className="gradient-text">JaKtA</span>?</Heading>
        <p>
          JaKtA is a framework for programming intelligent agents with the Belief-Desire-Intention (BDI)
          architecture, built in <a target="_blank" rel="noreferrer" href="https://kotlinlang.org/">Kotlin</a>.
          Agents are plain Kotlin code, so there is no separate agent language to learn and you keep the tools you
          already love: your IDE, build tool, debugger and libraries.
        </p>
        <p>
          If you use JaKtA in a research project, please <a href="#about">cite it</a>.
        </p>
      </div>
      <div className={styles.infoLogo}>
        <img src="/img/jakta-logo.png" alt="JaKtA Logo" />
      </div>
    </div>
  );
}

// The icon is a mask over the brand gradient, so the single-colour SVG takes the gradient.
function FeatureCard({ icon, title, children }: { icon: string; title: string; children: React.ReactNode }) {
  const mask = `url(${icon}) center / contain no-repeat`;
  return (
    <div className={styles.featureCard}>
      <div className={styles.featureCardContent}>
        <span className={styles.featureIcon} style={{mask, WebkitMask: mask}} aria-hidden="true" />
        <Heading as="h3">{title}</Heading>
        {children}
      </div>
    </div>
  );
}

export default function FeaturesSection() {
  return (
    <section className={styles.featuresSection} id={"features"}>
      <InfoCard />
      <FeatureCard icon="/img/blending.svg" title="Multi Paradigm">
        <p>Paradigm blending: mix Functional and Object-Oriented programming within your MAS codebase</p>
      </FeatureCard>
      <FeatureCard icon="/img/multiplatform.svg" title="Multi Platform">
        <p>Write once, run on the JVM, in JavaScript, and natively on Linux, Windows, macOS and iOS</p>
      </FeatureCard>
      <FeatureCard icon="/img/configuration.svg" title="Configurable">
        <p>Plug in the knowledge representation and the execution model, from coroutines to simulations</p>
      </FeatureCard>
    </section>
  );
}
