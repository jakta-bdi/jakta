import React from 'react';

// Formatted at build time by src/plugins/bibtex-loader.js
// @ts-ignore
import sections from '../../../static/jakta-references.bib';

const PublicationsList: React.FC = () => (
	<>
		{(sections as {year: string; citations: string[]}[]).map(({year, citations}) => (
			<section key={year} style={{ marginBottom: '2.5rem' }}>
				<h2>{year}</h2>
				<ul style={{ paddingLeft: '1.5em' }}>
					{citations.map((citation, idx) => (
						<li key={idx} style={{ marginBottom: '1rem'}}
								dangerouslySetInnerHTML={{ __html: citation }} />
					))}
				</ul>
			</section>
		))}
	</>
);

export default PublicationsList;
