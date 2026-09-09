import React, { useEffect, useState } from 'react';
import styles from './HomePageNavbar.module.css';

const sections = [
	{ id: 'features', label: 'Features' },
	{ id: 'team', label: 'Team' },
	{ id: 'about', label: 'About' },
];

const HomePageNavbar: React.FC = () => {
	const [activeSection, setActiveSection] = useState<string>('features');
	const [scrolled, setScrolled] = useState<boolean>(false);

	useEffect(() => {
		const handleScroll = () => {
			let current = sections[0].id;
			for (const section of sections) {
				const el = document.getElementById(section.id);
				if (el) {
					const rect = el.getBoundingClientRect();
					if (rect.top <= 100 && rect.bottom > 100) {
						current = section.id;
						break;
					}
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
		<nav className={scrolled ? `${styles.navbar} ${styles.solid}` : styles.navbar}>
			<ul className={styles.navList}>
				{sections.map((section) => (
					<li className={styles.navItem} key={section.id}>
						<a
							href={`#${section.id}`}
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
