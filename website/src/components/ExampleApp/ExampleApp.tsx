import React from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './ExampleApp.module.css';

// A Compose Multiplatform example built for the browser by scripts/build-examples.sh, one per showcase page.
// The apps adapt their layout to the width they get, so the frame only sets how tall they are.
export default function ExampleApp({ name, title }: { name: string; title: string }) {
  const src = useBaseUrl(`/examples/${name}/index.html`);

  return (
    <>
      <div className={styles.frame}>
        <iframe className={styles.app} src={src} title={title} />
      </div>
      <p className={styles.newTab}>
        <a href={src} target="_blank" rel="noreferrer">Open {title} in a new tab</a>
      </p>
    </>
  );
}
