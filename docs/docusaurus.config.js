// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'CI Aid for GitLab',
  tagline: 'The best aid you can get in IntelliJ IDEs for working with GitLab CI YAML! 🚀',

  url: 'https://deeepamin.github.io',
  baseUrl: '/gitlab-ci-aid/',
  trailingSlash: false,
  organizationName: 'deeepamin',
  projectName: 'gitlab-ci-aid',
  deploymentBranch: 'gh-pages',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      // image: 'img/docusaurus-social-card.jpg',
      navbar: {
        title: 'CI Aid for GitLab',
        logo: {
          alt: 'Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: 'Documentation',
          },
          {
            href: 'https://github.com/deeepamin/gitlab-ci-aid/',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      colorMode: {
        defaultMode: 'dark',
        respectPrefersColorScheme: false,
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Resources',
            items: [
              {
                label: 'License',
                href: 'https://github.com/deeepamin/gitlab-ci-aid/blob/main/LICENSE',
              },
              {
                label: 'Releases',
                href: 'https://github.com/deeepamin/gitlab-ci-aid/releases',
               },
            ],
          },
          {
            title: 'Support',
            items: [
              {
                label: 'Rate and Share the plugin',
                href: 'https://plugins.jetbrains.com/plugin/25859-ci-aid-for-gitlab'
              },
              {
                label: 'Buy me a coffee',
                href: 'https://ko-fi.com/deeepamin',
              },
              {
                label: 'Sponsor on GitHub',
                href: 'https://github.com/sponsors/deeepamin',
              },
            ],
          },
        ],
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;
