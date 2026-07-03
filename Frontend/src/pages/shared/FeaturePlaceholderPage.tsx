interface FeaturePlaceholderPageProps {
  title: string;
  description: string;
}

export const FeaturePlaceholderPage = ({ title, description }: FeaturePlaceholderPageProps) => (
  <div className="space-y-4">
    <h2 className="text-2xl font-semibold text-slate-900">{title}</h2>
    <div className="rounded-lg border border-dashed border-slate-300 bg-white p-8 text-sm text-slate-600 shadow-sm">
      {description}
    </div>
  </div>
);