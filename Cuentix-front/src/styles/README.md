# Cuentix design system

The global design system is loaded by `src/styles.scss` and is divided into:

- `_tokens.scss`: semantic color, typography, spacing, sizing, radius, shadow, and transition tokens.
- `_typography.scss`: Poppins font loading and the global type scale.
- `_material.scss`: Angular Material token overrides for fields, dialogs, menus, snack bars, and toggles.
- `_utilities.scss`: global reset, focus treatment, numeric formatting, surfaces, and reduced-motion support.

Feature styles should consume `--ds-*` tokens instead of defining new global colors or spacing values. The aliases in `_tokens.scss` are temporary compatibility names for existing feature styles and should be replaced with their `--ds-*` equivalent when those files are next changed.
