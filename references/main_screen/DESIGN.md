---
name: Financial Wellness System
colors:
  surface: '#fbf9f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fbf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f2'
  surface-container: '#f0eded'
  surface-container-high: '#eae8e7'
  surface-container-highest: '#e4e2e1'
  on-surface: '#1b1c1c'
  on-surface-variant: '#414847'
  inverse-surface: '#303030'
  inverse-on-surface: '#f3f0f0'
  outline: '#717977'
  outline-variant: '#c0c8c6'
  surface-tint: '#3e6560'
  primary: '#3e6560'
  on-primary: '#ffffff'
  primary-container: '#78a19b'
  on-primary-container: '#0b3733'
  inverse-primary: '#a5cfc8'
  secondary: '#7d5546'
  on-secondary: '#ffffff'
  secondary-container: '#fec8b6'
  on-secondary-container: '#7a5243'
  tertiary: '#6b5b50'
  on-tertiary: '#ffffff'
  tertiary-container: '#a89689'
  on-tertiary-container: '#3b2f25'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c0ebe4'
  primary-fixed-dim: '#a5cfc8'
  on-primary-fixed: '#00201d'
  on-primary-fixed-variant: '#254d49'
  secondary-fixed: '#ffdbcf'
  secondary-fixed-dim: '#efbba9'
  on-secondary-fixed: '#301409'
  on-secondary-fixed-variant: '#623e30'
  tertiary-fixed: '#f4dfd0'
  tertiary-fixed-dim: '#d7c3b5'
  on-tertiary-fixed: '#241911'
  on-tertiary-fixed-variant: '#52443a'
  background: '#fbf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e1'
typography:
  display-lg:
    fontFamily: Manrope
    fontSize: 40px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '600'
    lineHeight: '1.4'
    letterSpacing: 0.05em
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  data-mono:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
  label-xs:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: 0.08em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 40px
  xl: 64px
  gutter: 20px
  container-padding: 32px
---

## Brand & Style

The design system is built on the philosophy of "Warm Minimalism." It moves away from the cold, clinical feel of traditional fintech and instead embraces a calm, approachable aesthetic that encourages long-term financial engagement. The target audience includes professionals and mindful consumers who value clarity and a stress-free overview of their data.

The UI evokes a sense of order and serenity through generous whitespace, a high-key palette, and a focus on information density that doesn't feel overwhelming. By combining structural precision with a soft, natural color story, the design system transforms complex data tracking into a tactile, editorial experience.

## Colors

The palette is derived from natural, earthy elements to provide a professional yet soothing backdrop for financial data.

- **Primary (Teal):** Used for "Actual" data points, growth trends, and primary actions. It represents stability and progress.
- **Secondary (Coral/Sand):** Used for "Budgeted" benchmarks and attention-grabbing accents. It provides a warm contrast to the teal.
- **Tertiary (Warm Sand):** Dedicated to header backgrounds, category tags, and structural divisions.
- **Neutral:** A deep charcoal-brown is used for text instead of pure black to maintain the softness of the interface.
- **Background:** A creamy off-white (`#FCF8F5`) serves as the canvas, reducing eye strain and enhancing the "airy" feel.

## Typography

The design system utilizes **Manrope** for its exceptional legibility and modern, geometric character. It balances the technical requirements of a dashboard with a friendly, open curve.

- **Information Hierarchy:** Headers use uppercase styling with increased letter spacing to create a clear "anchor" for sections.
- **Numerical Data:** For financial figures, the system defaults to tabular figures (`tnum`) to ensure decimal points align perfectly in vertical columns.
- **Clarity:** Line heights are kept generous to prevent data-heavy tables from feeling cramped.

## Layout & Spacing

The layout follows a **12-column fluid grid** designed to maximize the "airy" aesthetic.

- **Margins & Gutters:** Large 32px external margins and 20px gutters ensure that content blocks have room to breathe.
- **Responsive Behavior:** 
  - **Desktop:** Multi-column dashboard view with 3-4 main widgets per row.
  - **Tablet:** Reflows to a 2-column layout.
  - **Mobile:** Single column with simplified navigation; horizontal scrolling is enabled specifically for wide data tables.
- **Rhythm:** All vertical spacing must be a multiple of the 8px base unit to maintain a rigorous visual cadence.

## Elevation & Depth

This design system avoids traditional heavy drop shadows in favor of **Tonal Layers** and **Subtle Outlines**.

- **Layering:** Depth is communicated through color blocking. Backgrounds are the lowest layer (`#FCF8F5`), while active widgets and cards sit on a pure white surface (`#FFFFFF`).
- **Outlines:** Containers use a thin 1px border in a muted sand tone (`#E5E0DA`). This provides structure without the "weight" of a shadow.
- **Interaction:** On hover, a very soft, diffused shadow (low opacity teal or neutral) may be used to indicate interactivity, but it should remain almost imperceptible.

## Shapes

The shape language is "Soft Professional." Elements use a subtle corner radius that feels intentional and modern without becoming overly playful or "bubbly."

- **Cards:** Use `rounded-lg` (0.5rem) to differentiate main sections.
- **Buttons & Inputs:** Use the base `rounded` (0.25rem) for a crisp, organized appearance.
- **Data Visuals:** Donut charts and progress bars should use rounded caps to maintain consistency with the overall soft geometry.

## Components

### Data Tables
Tables are the core of the design system. Headers should have a Tertiary (`#EBD6C8`) background with uppercase `label-xs` typography. Rows are separated by 1px horizontal lines only—no vertical borders—to maintain the clean flow of information.

### Cards
Every widget must be wrapped in a card with a white background and a subtle border. The title area of the card should be visually distinct, using either the tertiary sand color or a clear typography weight change.

### Buttons
Primary buttons use the Teal accent with white text. Secondary buttons are "Ghost" style, using the Coral accent for the border and text, creating a clear but softer secondary action.

### Input Fields
Inputs are minimalist: a bottom border only or a very light 4-sided border. When focused, the border transitions to Teal. Labels sit above the field in `label-xs` style.

### Data Visualization
- **Donut Charts:** Use a thick stroke with a centered total value.
- **Progress Bars:** Use Teal for "Actual" progress and a lighter tint of the same color for the background track.
- **Bar Charts:** Use a mix of Teal and Coral to distinguish between Budgeted vs. Actual spending.