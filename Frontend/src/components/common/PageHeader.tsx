interface PageHeaderProps {
  title: string;
  description?: string;
}

export const PageHeader = ({ title, description }: PageHeaderProps) => (
  <div className="mb-6">
    <h2 className="text-3xl font-bold text-slate-900 tracking-tight">{title}</h2>
    {description ? <p className="mt-2 text-base text-slate-600 leading-relaxed">{description}</p> : null}
  </div>
);
