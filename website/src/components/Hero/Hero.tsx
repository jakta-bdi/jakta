import React from 'react';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import styles from './Hero.module.css';

export default function Hero() {
  return (
    <header className={styles.heroBanner}>
      <div className={styles.heroBg} />
      <div className={styles.heroShade} />
      <div className={styles.heroContent}>
        <div className={styles.heroText}>
          <Heading as="h1" className={styles.heroTitle}>
            BDI agents,
            <br />
            <span className={styles.gradientText}>in plain Kotlin.</span>
          </Heading>
          <p className={styles.heroSubtitle}>
            Build type-safe, reactive, and proactive systems.{' '}
            <br />
            Multi-paradigm and multi-platform by design.
          </p>
          <div className={styles.heroButtons}>
            <Link className="pill-button pill-button--primary" to="/docs/tutorials/">
              Get Started
            </Link>
            <Link className={`pill-button pill-button--outline ${styles.onPhoto}`} to="/docs/intro">
              Learn More
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}
