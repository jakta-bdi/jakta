import React from 'react';
import * as citationJs from '@citation-js/core';
import '@citation-js/plugin-bibtex';
import '@citation-js/plugin-csl';

// @ts-ignore
import bibtexText from '!!raw-loader!../../../static/jakta-references.bib';

const Cite = citationJs.Cite || (citationJs as any).default.Cite;

const PublicationsList: React.FC = () => {
	let sections: Record<string, string[]> = {};
	let years: string[] = [];
	let error: string | null = null;
	try {
		const cite = new Cite(bibtexText);
		const data = cite.get({ type: 'json' });
		// Group entries by year
		const grouped: Record<string, { entry: any; citation: string }[]> = {};
		data.forEach((entry: any) => {
			let year = '';
			if (entry.issued && entry.issued['date-parts'] && entry.issued['date-parts'][0]) {
				year = String(entry.issued['date-parts'][0][0]);
			} else if (entry.year) {
				year = String(entry.year);
			} else {
				year = 'Unknown';
			}
			const singleCite = new Cite([entry]);
			let citation = singleCite.format('bibliography', {
				format: 'html',
				template: 'apa',
				lang: 'en-US',
			});
			// Replace plain URLs in citation text with clickable links
			if (entry.URL) {
				const urlRegex = new RegExp(`(https?://[^\s<]+)`, 'g');
				citation = citation.replace(urlRegex, `<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>`);
			}
			if (!grouped[year]) grouped[year] = [];
			grouped[year].push({ entry, citation });
		});
		// Sort years descending
		years = Object.keys(grouped).sort((a, b) => b.localeCompare(a, undefined, { numeric: true }));
		// Prepare sections
		years.forEach((year) => {
			sections[year] = grouped[year].map((item) => item.citation);
		});
	} catch (e: any) {
		error = e.message;
	}

	return (
		<>
			{error && <p style={{ color: 'red' }}>{error}</p>}
			{years.map((year) => (
				<section key={year} style={{ marginBottom: '2.5rem' }}>
					<h2>{year}</h2>
					<ul style={{ paddingLeft: '1.5em' }}>
						{sections[year].map((citation, idx) => (
							<li key={idx} style={{ marginBottom: '1rem'}}
									dangerouslySetInnerHTML={{ __html: citation }} />
						))}
					</ul>
				</section>
			))}
		</>
	);
};

export default PublicationsList;
