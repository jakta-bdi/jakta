import React from 'react';
import Link from '@docusaurus/Link';
import useRouteContext from '@docusaurus/useRouteContext';
import {BlogPostProvider} from '@docusaurus/plugin-content-blog/client';
import BlogPostItem from '@theme/BlogPostItem';
import type {Props} from '@theme/BlogPostItems';
// @ts-ignore: loaded by the yaml-loader plugin in docusaurus.config.ts
import tags from '@site/blog/tags.yml';
import styles from './styles.module.css';

type TagDefinition = {label: string; permalink: string; description?: string};

// Ejected from @docusaurus/theme-classic to render posts as a card grid with a topic bar on top.
// The same component renders the /releases blog instance (see docusaurus.config.ts) as a compact changelog.
export default function BlogPostItems({
  items,
  component: BlogPostItemComponent = BlogPostItem,
}: Props): JSX.Element {
  if (useRouteContext().plugin.id === 'releases') {
    return (
      <div className={styles.releases}>
        {items.map(({content: BlogPostContent}) => (
          <BlogPostProvider
            key={BlogPostContent.metadata.permalink}
            content={BlogPostContent}>
            <BlogPostItemComponent className={styles.release}>
              <BlogPostContent />
            </BlogPostItemComponent>
          </BlogPostProvider>
        ))}
      </div>
    );
  }
  return (
    <>
      <nav className={styles.topics} aria-label="Blog topics">
        <Link className={styles.topic} to="/blog">All posts</Link>
        {Object.values(tags as Record<string, TagDefinition>).map((tag) => (
          <Link
            key={tag.permalink}
            className={styles.topic}
            to={`/blog/tags${tag.permalink}`}
            title={tag.description}>
            {tag.label}
          </Link>
        ))}
        <Link className={styles.topic} to="/releases" title="Release notes of every JaKtA version">
          Releases →
        </Link>
      </nav>
      <div className={styles.grid}>
        {items.map(({content: BlogPostContent}) => (
          <BlogPostProvider
            key={BlogPostContent.metadata.permalink}
            content={BlogPostContent}>
            <BlogPostItemComponent className={styles.card}>
              <BlogPostContent />
            </BlogPostItemComponent>
          </BlogPostProvider>
        ))}
      </div>
    </>
  );
}
