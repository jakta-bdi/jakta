
import React from 'react';
import styles from './CallToAction.module.css';
import { Icon } from '@iconify/react';

const CTA_URL = 'https://github.com/jakta-bdi';

export default function CallToAction() {
  return (
    <section className={styles.callToActionSection}>
      <h2>Join the JaKtA Community!</h2>
      <p>
        Be a part of our open-source journey. Collaborate, contribute, and connect with fellow MAS and BDI enthusiasts.
      </p>
      <a
        href={CTA_URL}
        target="_blank"
        rel="noopener noreferrer"
        className={styles.ctaButton}
      >
        <span className={styles.githubLogo} aria-hidden="true">
          <Icon icon="mdi:github" width="30" height="30" style={{verticalAlign: 'middle', marginRight: '0.5rem'}} />
        </span>
        Join on GitHub
      </a>
    </section>
  );
}
