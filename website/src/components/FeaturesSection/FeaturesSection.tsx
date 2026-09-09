import React from 'react';
import ConfigurationIconRaw from '@site/static/img/configuration.svg';
import BlendingIconRaw from '@site/static/img/blending.svg';
import MultiplatformIconRaw from '@site/static/img/multiplatform.svg';
// Remove width/height attributes from SVGs by wrapping them
function stripSizeProps(IconComponent: React.ComponentType<any>) {
  return (props: any) => <IconComponent {...props} width={undefined} height={undefined} />;
}

const ConfigurationIcon = stripSizeProps(ConfigurationIconRaw);
const BlendingIcon = stripSizeProps(BlendingIconRaw);
const MultiplatformIcon = stripSizeProps(MultiplatformIconRaw);
import Heading from '@theme/Heading';
import styles from './FeaturesSection.module.css';

function InfoCard() {
  return (
    <div className={styles.infoSection}>
      <div className={styles.infoText}>
        <Heading as="h2">What is JaKtA?</Heading>
        <p>
          JaKtA is an agent-oriented programming framework for building robust
          intelligent agents using the Belief-Desire-Intention (BDI) cognitive
          architecture.
          It's implemented in <a target="_blank" href="https://kotlinlang.org/">Kotlin</a> and aims to simplify the development of complex agent-based systems by leveraging the language's modern features and concise syntax.
          Thanks to Kotlin multi-paradigm design, JaKtA agents seamlessly interoperate with Object-Oriented and Functional Programming, making BDI agents approachable for a wide range of developers.
          Kotlin multiplatform support also opens the door for JaKtA agents to run on various platforms, from servers to mobile devices.
        </p>
        <p>
          If you intend to use JaKtA for a research project, make sure to <a href="#about">cite it</a>.
        </p>
      </div>
      <div className={styles.infoLogo}>
        <img src="/img/jakta-logo.png" alt="JaKtA Logo" />
      </div>
    </div>
  );
}

function FeatureCard({ Icon, title, children }: { Icon?: React.ComponentType<{ className?: string }>; title: string; children: React.ReactNode }) {
  return (
    <div className={styles.featureCard}>
      <div className={styles.featureCardContent}>
        {Icon ? <Icon className={styles.featureIcon} /> : null}
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
      <FeatureCard Icon={BlendingIcon} title="Paradigm Blending">
        <p>Mix Functional and Object-Oriented programming within your MAS codebase</p>
      </FeatureCard>
      <FeatureCard Icon={MultiplatformIcon} title="Multi Platform">
        <p>Write once, run on JVM, JS, Native, Android, and iOS</p>
      </FeatureCard>
      <FeatureCard Icon={ConfigurationIcon} title="Configurable">
        <p>Control concurrency and knowledge representation models</p>
      </FeatureCard>
    </section>
  );
}
