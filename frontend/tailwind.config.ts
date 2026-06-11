import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui"]
      },
      colors: {
        abyss: "#07111f",
        panel: "rgba(15, 23, 42, 0.72)",
        cyanGlow: "#22d3ee",
        blueGlow: "#3b82f6"
      },
      boxShadow: {
        glow: "0 0 40px rgba(34, 211, 238, 0.18)",
        card: "0 24px 70px rgba(0, 0, 0, 0.28)"
      }
    }
  },
  plugins: []
} satisfies Config;
