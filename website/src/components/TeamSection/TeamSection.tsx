import React from 'react';
import styles from './TeamSection.module.css';

import authors from '../../../blog/authors.yml';
import { Icon } from '@iconify/react/dist/iconify.js';

type Author = {
  name: string;
  title: string;
  image_url: string;
  socials: {
    github: string;
  };
};


export default function TeamSection() {
  return (
    <section className={styles.teamSection} id="team">
      <h2>Meet the JaKtA Team</h2>

        <p className="text-lg padding--md">
            We are a group of researchers from the  
            <a 
                href="https://www.unibo.it/en" 
                target="_blank" 
                rel="noopener noreferrer" 
            > University of Bologna</a>,  
            in the  
            <a 
                href="https://disi.unibo.it/en" 
                target="_blank" 
                rel="noopener noreferrer" 
            > Department of Computer Science and Engineering (DISI)</a>,  
            based in Cesena, Italy <Icon inline={true} icon="twemoji:flag-italy" />
        </p>

      <div className={styles.teamGrid}>
        {Object.entries(authors).map(([key, member]) => {
          const author = member as Author;
          return (
            <div key={key} className={styles.teamMember}>
              <img src={author.image_url} alt={author.name} className={styles.avatar} />
              <h3>{author.name}</h3>
              <p>{author.title}</p>
              <a href={`https://github.com/${author.socials.github}`} target="_blank" rel="noopener noreferrer" aria-label={`${author.name}'s GitHub`}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.84 1.236 1.84 1.236 1.07 1.834 2.809 1.304 3.495.997.108-.775.418-1.305.762-1.605-2.665-.305-5.466-1.334-5.466-5.93 0-1.31.469-2.381 1.236-3.221-.124-.303-.535-1.523.117-3.176 0 0 1.008-.322 3.301 1.23a11.52 11.52 0 013.003-.404c1.018.005 2.045.138 3.003.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.873.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.804 5.624-5.475 5.921.43.371.823 1.102.823 2.222 0 1.606-.014 2.898-.014 3.293 0 .322.218.694.825.576C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
              </a>
            </div>
          );
        })}
      </div>

      <div className="padding-top--lg">
        <p className="text-lg">
            Discover more about what we and our colleagues do on the  
            <a 
            href="https://pslab-unibo.github.io/"
            target="_blank"
            > Pervasive Software Lab </a>
            website!
        </p>
      </div>
      
    </section>
  );
}
