import React, {type ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import {
  PageMetadata,
  HtmlClassNameProvider,
  ThemeClassNames,
  translateTagsPageTitle,
} from '@docusaurus/theme-common';
import BlogLayout from '@theme/BlogLayout';
import SearchMetadata from '@theme/SearchMetadata';
import Heading from '@theme/Heading';
import type {Props} from '@theme/BlogTagsListPage';
import styles from './styles.module.css';

// Ejected from @docusaurus/theme-classic: one card per topic instead of an A-Z list.
export default function BlogTagsListPage({tags, sidebar}: Props): ReactNode {
  const title = translateTagsPageTitle();
  const sorted = [...tags].sort((a, b) => b.count - a.count);
  return (
    <HtmlClassNameProvider
      className={clsx(
        ThemeClassNames.wrapper.blogPages,
        ThemeClassNames.page.blogTagsListPage,
      )}>
      <PageMetadata title={title} />
      <SearchMetadata tag="blog_tags_list" />
      <BlogLayout sidebar={sidebar}>
        <Heading as="h1" className={styles.title}>Topics</Heading>
        <div className={styles.grid}>
          {sorted.map((tag) => (
            <Link key={tag.permalink} to={tag.permalink} className={styles.card}>
              <span className={styles.label}>{tag.label}</span>
              <span className={styles.count}>
                {tag.count} {tag.count === 1 ? 'post' : 'posts'}
              </span>
              {tag.description && <p className={styles.description}>{tag.description}</p>}
            </Link>
          ))}
        </div>
      </BlogLayout>
    </HtmlClassNameProvider>
  );
}
