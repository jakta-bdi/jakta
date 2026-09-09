import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import styles from './Hero.module.css';

export default function Hero() {
  return (
    <header className={clsx(styles.heroBanner)}>
      <div className={styles.heroBg}>
        <div className={styles.heroOverlay} />
      </div>
      <div className={styles.heroContent}>
        <Heading as="h1" className={styles.heroTitle}>
          JaKtA: BDI Agents Made Simple
        </Heading>
        <p className={styles.heroSubtitle}>
          Build robust, reactive, and proactive systems with Kotlin.
          <br />
          Multi-paradigm and multi-platform by design.
        </p>
        <div className={styles.heroButtons}>
          <Link className={styles.primaryBtn} to="/docs/getting-started/">
            Get Started
          </Link>
          <Link className={styles.secondaryBtn} to="/docs/intro">
            Learn More
          </Link>
        </div>
      </div>
    </header>
  );
}
