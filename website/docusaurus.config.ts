import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const githubURL : string = 'https://github.com/jakta-bdi';
const websiteURL : string = 'https://jakta-bdi.github.io';

const config: Config = {
  title: 'JaKtA',
  tagline: 'A BDI agent programming framework',
  favicon: 'img/favicon/favicon.ico',

  // Set the production url of your site here
  url: websiteURL,
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'jakta-bdi', // Usually your GitHub org/user name.
  projectName: 'jakta', // Usually your repo name.
  trailingSlash: false,

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  plugins: [
    function yamlLoader(context, options) {
      return {
        name: "yaml-loader",
        configureWebpack(config, isServer) {
          return {
            module: {
              rules: [
                {
                  test: /\.ya?ml$/,
                  use: "yaml-loader",
                },
              ],
            },
          };
        },
      };
    },
    function bibtexLoader(context, options) {
    return {
      name: "bibtex-loader",
      configureWebpack(config, isServer) {
        return {
          module: {
            rules: [
              {
                test: /\.bib$/,
                use: "raw-loader",
              },
            ],
          },
        };
      },
    };
  },
  ],

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          editUrl: websiteURL,
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          editUrl: websiteURL,
          // Useful options to enforce blogging best practices
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/jakta-social.jpg',
    colorMode: {
      defaultMode: 'light',
      disableSwitch: false,
      respectPrefersColorScheme: false,
    },
    // announcementBar: {
    //   id: 'underconstruction',
    //   content:
    //     'This website is currently under construction. Please check back later for updates.',
    //   backgroundColor: '#FFFF8F',
    //   textColor: 'black',
    //   isCloseable: false,
    // },
    navbar: {
      hideOnScroll: false,
      title: 'JaKtA',
      logo: {
        alt: 'JaKtA Logo',
        src: 'img/jakta-logo.png',
        className: 'navbar-logo',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          to: '/publications',
          label: 'Publications', 
          position: 'left'
        },
        {
          to: '/blog',
          label: 'Blog', 
          position: 'left'
        },
        {
          type: 'search',
          position: 'right',
        },
        {
          href: githubURL,
          position: 'right',
          className: 'header-github-link',
          'aria-label': 'GitHub repository',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Introduction',
              to: '/docs/intro',
            },
            {
              label: 'Getting Started',
              to: '/docs/getting-started',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'GitHub',
              href: githubURL,
            },
            {
              label: 'Q&A Forum',
              href: 'https://github.com/orgs/jakta-bdi/discussions',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'Blog',
              to: '/blog',
            },
            {
              label: 'Pervasive Software Lab',
              to: 'https://pslab-unibo.github.io/',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} JaKtA, Org. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.vsDark,
      defaultLanguage: 'kotlin',
    },
    algolia: {
      appId: 'VYWAYTYENC',
      apiKey: 'e71e39c23b9e037ad639a9295d3dad4c',
      indexName: 'jakta-bdiio',

      contextualSearch: true,

    },
    customFields: {
      githubURL: githubURL,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
