import clsx from 'clsx';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';

import Heading from '@theme/Heading';
import styles from './index.module.css';
import {Carousel} from "react-responsive-carousel";
import 'react-responsive-carousel/lib/styles/carousel.min.css';
import useBaseUrl from "@docusaurus/useBaseUrl";

const screenshotFiles = [
    'Autocomplete_syntax_highlighting.png',
    'Navigation_quick_fix_demo.gif',
    'Shell_script_injection.png',
    'Undefined_script.png',
    'Undefined_stage.png',
];

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
        <main>
            <div className="container margin-vert--lg">
                <Carousel
                    showArrows={true}
                    showThumbs={false}
                    showStatus={false}
                    infiniteLoop
                    autoPlay
                    interval={4000}
                    swipeable
                    emulateTouch
                    stopOnHover={true}
                    transitionTime={500}
                >
                    {screenshotFiles.map((filename, index) => (
                        <div key={filename}>
                            <img src={useBaseUrl(`/img/screenshots/${filename}`)}
                                 alt={`Screenshot ${index + 1}`}
                                 style={{ borderRadius: '1rem', maxHeight: '500px', objectFit: 'contain' }}/>
                        </div>
                    ))}
                </Carousel>
            </div>
        </main>
    </Layout>
  );
}
