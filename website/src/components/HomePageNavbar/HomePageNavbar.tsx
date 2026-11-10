import React, { useEffect, useRef, useState } from 'react';
import styles from './HomePageNavbar.module.css';

const sections = [
	{ id: 'features', label: 'Features' },
	{ id: 'showcase', label: 'Showcase' },
	{ id: 'team', label: 'Team' },
	{ id: 'about', label: 'About' },
];

const HomePageNavbar: React.FC = () => {
	const [activeSection, setActiveSection] = useState<string>('features');
	const [scrolled, setScrolled] = useState<boolean>(false);

	const navRef = useRef<HTMLElement>(null);

	useEffect(() => {
		const handleScroll = () => {
			// The active section is the last one whose top has passed a line just below this bar, measured from the bar
			// so it matches where the anchors land (their scroll margin); at the bottom of the page, the last section.
			const line = (navRef.current?.getBoundingClientRect().bottom ?? 0) + 120;
			const atBottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2;
			let current = sections[0].id;
			for (const section of sections) {
				const el = document.getElementById(section.id);
				if (el && (el.getBoundingClientRect().top <= line || atBottom)) {
					current = section.id;
				}
			}
			setActiveSection(current);
			setScrolled(window.scrollY > 10);
		};
		window.addEventListener('scroll', handleScroll);
		handleScroll();
		return () => window.removeEventListener('scroll', handleScroll);
	}, []);

	return (
		<nav ref={navRef} className={scrolled ? `${styles.navbar} ${styles.solid}` : styles.navbar}>
			<ul className={styles.navList}>
				{sections.map((section) => (
					<li className={styles.navItem} key={section.id}>
						<a
							href={`#${section.id}`}
							// One smooth scroll, ours: following the hash natively also makes Docusaurus scroll to it again
							// on the route change, and in Firefox the two scrolls fight and can stall. pushState keeps one
							// history entry per section without telling the router, so Back still steps through them.
							onClick={(e) => {
								e.preventDefault();
								document.getElementById(section.id)?.scrollIntoView({ behavior: 'smooth' });
								window.history.pushState(null, '', `#${section.id}`);
							}}
							className={
								`${styles.navLink} ${activeSection === section.id ? styles.active : ''}`
							}
						>
							{section.label}
						</a>
					</li>
				))}
			</ul>
		</nav>
	);
};

export default HomePageNavbar;
