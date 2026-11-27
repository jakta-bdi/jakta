import React from 'react';
import useRouteContext from '@docusaurus/useRouteContext';
import BlogSidebar from '@theme-original/BlogSidebar';
import type {Props} from '@theme/BlogSidebar';
import styles from './styles.module.css';

// Wrapped from @docusaurus/theme-classic: in /releases, the sidebar entries scroll to the release on the
// single list page (see docusaurus.config.ts and BlogPostItems) instead of opening its own page.
export default function BlogSidebarWrapper({sidebar}: Props): JSX.Element {
  if (useRouteContext().plugin.id !== 'releases' || !sidebar) {
    return <BlogSidebar sidebar={sidebar} />;
  }
  const items = sidebar.items.map((item) => {
    const slash = item.permalink.lastIndexOf('/');
    return {...item, permalink: `${item.permalink.slice(0, slash)}#${item.permalink.slice(slash + 1)}`};
  });
  return (
    <div className={styles.releases}>
      <BlogSidebar sidebar={{...sidebar, items}} />
    </div>
  );
}
