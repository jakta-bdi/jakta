import React, { useEffect, useRef, useState } from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './ExampleApp.module.css';

// The apps are laid out for a desktop window: render them at least this wide, scaled down to fit narrower pages.
const APP_WIDTH = 1200;
const APP_HEIGHT = 800;

// A Compose Multiplatform example built for the browser by scripts/build-examples.sh, one per showcase page.
export default function ExampleApp({ name, title }: { name: string; title: string }) {
  const [width, setWidth] = useState(APP_WIDTH);
  const frame = useRef<HTMLDivElement>(null);
  const src = useBaseUrl(`/examples/${name}/index.html`);

  useEffect(() => {
    const observer = new ResizeObserver(([entry]) => setWidth(entry.contentRect.width));
    observer.observe(frame.current!);
    return () => observer.disconnect();
  }, []);

  const scale = Math.min(1, width / APP_WIDTH);

  return (
    <>
      <div ref={frame} className={styles.frame} style={{ height: APP_HEIGHT * scale }}>
        <iframe
          className={styles.app}
          src={src}
          title={title}
          style={{ width: Math.max(width, APP_WIDTH), height: APP_HEIGHT, transform: `scale(${scale})` }}
        />
      </div>
      <p className={styles.newTab}>
        <a href={src} target="_blank" rel="noreferrer">Open {title} in a new tab</a>
      </p>
    </>
  );
}
