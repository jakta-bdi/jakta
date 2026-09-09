import React from 'react';
import styles from './AboutSection.module.css';
import CodeBlock from '@theme/CodeBlock';

export default function AboutSection() {
  return (
    <section className={styles.aboutSection} id="about">
      <h2>About JaKtA</h2>
      <div className={styles.aboutContent}>

        {/* <div className={styles.aboutGrid}>
              <div className={styles.aboutCard}>
                <h4>What is JaKtA?</h4>

              </div>
              <div className={styles.aboutCard}>
                <h4>How does JaKtA work?</h4>

              </div>
              <div className={styles.aboutCard}>
                <h4>Why use JaKtA?</h4>

              </div>
        </div> */}

      
        <div className={styles.citeCard}>
          <div className={styles.citeHeader}>
            <p>Please make sure to cite JaKtA if you use it in a research project! <br/>
            If you're interested in research developments check out the other <a href="/publications">publications</a>.</p>
          </div>

          <CodeBlock showLineNumbers={false}>
  {`@article{DBLP:journals/sncs/BaiardiBCP24,
    author       = {Martina Baiardi and Samuele Burattini and Giovanni Ciatto and Danilo Pianini},
    title        = {Blending {BDI} Agents 
                    with Object-Oriented and Functional Programming with JaKtA},
    journal      = {{SN} Comput. Sci.},
    volume       = {5},
    number       = {8},
    pages        = {1003},
    year         = {2024},
    url          = {https://doi.org/10.1007/s42979-024-03244-y},
    doi          = {10.1007/S42979-024-03244-Y},
    timestamp    = {Mon, 03 Mar 2025 22:23:02 +0100},
    biburl       = {https://dblp.org/rec/journals/sncs/BaiardiBCP24.bib},
    bibsource    = {dblp computer science bibliography, https://dblp.org}
  }`}
          </CodeBlock>
        </div>
      </div>
    </section>
  );
}